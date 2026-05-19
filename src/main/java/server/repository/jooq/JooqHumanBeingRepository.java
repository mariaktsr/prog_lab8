package server.repository.jooq;

import common.model.*;
import org.jooq.*;
import server.repository.HumanBeingRepository;
import server.repository.base.RepositoryException;
import server.repository.jooq.proxy.Transactional;
import common.model.User;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

import static org.jooq.impl.DSL.*;

public class JooqHumanBeingRepository implements HumanBeingRepository {

    private final DSLContext dsl;

    private static final Table<?> HB = table("human_beings");
    private static final Field<Long> HB_ID = field("id", Long.class);
    private static final Field<String> HB_NAME = field("name", String.class);
    private static final Field<Double> HB_X = field("x", Double.class);
    private static final Field<Long> HB_Y = field("y", Long.class);
    private static final Field<java.time.OffsetDateTime> HB_CREATION = field("creation_date", java.time.OffsetDateTime.class);
    private static final Field<Boolean> HB_REAL_HERO = field("real_hero", Boolean.class);
    private static final Field<Boolean> HB_TOOTHPICK = field("has_toothpick", Boolean.class);
    private static final Field<Long> HB_SPEED = field("impact_speed", Long.class);
    private static final Field<String> HB_WEAPON = field("weapon_type", String.class);
    private static final Field<String> HB_MOOD = field("mood", String.class);
    private static final Field<String> HB_CAR = field("car_name", String.class);
    private static final Field<String> HB_OWNER = field("owner_login", String.class);

    private static final Table<?> USERS = table("users");
    private static final Field<String> U_LOGIN = field("login", String.class);
    private static final Field<String> U_PASS = field("password_hash", String.class);

    public JooqHumanBeingRepository(DSLContext dsl) {
        if (dsl == null) throw new IllegalArgumentException("DSLContext не может быть null");
        this.dsl = dsl;
    }

    @Override
    public void initSchema() throws SQLException {
        dsl.execute("""
            CREATE TABLE IF NOT EXISTS users (
                login VARCHAR(255) PRIMARY KEY, password_hash VARCHAR(100) NOT NULL
            )
            """);
        dsl.execute("CREATE SEQUENCE IF NOT EXISTS human_beings_id_seq START WITH 1 INCREMENT BY 1");
        dsl.execute("""
            CREATE TABLE IF NOT EXISTS human_beings (
                id BIGINT PRIMARY KEY DEFAULT nextval('human_beings_id_seq'),
                name VARCHAR(255) NOT NULL, x DOUBLE PRECISION NOT NULL,
                y BIGINT NOT NULL CHECK (y > -228), creation_date TIMESTAMPTZ NOT NULL,
                real_hero BOOLEAN NOT NULL, has_toothpick BOOLEAN NOT NULL,
                impact_speed BIGINT NOT NULL CHECK (impact_speed > -428),
                weapon_type VARCHAR(50) NOT NULL, mood VARCHAR(50) NOT NULL,
                car_name VARCHAR(255), owner_login VARCHAR(255) NOT NULL REFERENCES users(login)
            )
            """);
    }

    @Override
    public List<HumanBeing> findAll() throws SQLException {
        return dsl.select()
                .from(HB)
                .join(USERS).on(HB_OWNER.eq(U_LOGIN))
                .orderBy(HB_ID)
                .fetch(this::mapRecord);
    }

    @Override
    @Transactional
    public boolean save(HumanBeing human) throws SQLException {
        try {
            validate(human);
        } catch (RepositoryException e) {
            throw new SQLException("Ошибка валидации при сохранении: " + e.getMessage(), e);
        }

        long newId = getNextId();
        human.setId(newId);

        return dsl.insertInto(HB)
                .columns(HB_ID, HB_NAME, HB_X, HB_Y, HB_CREATION, HB_REAL_HERO,
                        HB_TOOTHPICK, HB_SPEED, HB_WEAPON, HB_MOOD, HB_CAR, HB_OWNER)
                .values(human.getId(), human.getName(),
                        human.getCoordinates().getX(), human.getCoordinates().getY(),
                        human.getCreationDate().toOffsetDateTime(),
                        human.getRealHero(),
                        human.isHasToothpick(), human.getImpactSpeed(),
                        human.getWeaponType().name(), human.getMood().name(),
                        human.getCar() != null ? human.getCar().getName() : null,
                        human.getOwner().getLogin())
                .execute() > 0;
    }

    @Override
    @Transactional
    public boolean update(HumanBeing human) throws SQLException {
        try {
            validate(human);
        } catch (RepositoryException e) {
            throw new SQLException("Ошибка валидации при обновлении: " + e.getMessage(), e);
        }

        return dsl.update(HB)
                .set(HB_NAME, human.getName())
                .set(HB_X, human.getCoordinates().getX())
                .set(HB_Y, human.getCoordinates().getY())
                .set(HB_CREATION, human.getCreationDate().toOffsetDateTime())
                .set(HB_REAL_HERO, human.getRealHero())
                .set(HB_TOOTHPICK, human.isHasToothpick())
                .set(HB_SPEED, human.getImpactSpeed())
                .set(HB_WEAPON, human.getWeaponType().name())
                .set(HB_MOOD, human.getMood().name())
                .set(HB_CAR, human.getCar() != null ? human.getCar().getName() : null)
                .set(HB_OWNER, human.getOwner().getLogin())
                .where(HB_ID.eq(human.getId()))
                .execute() > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) throws SQLException {
        return dsl.deleteFrom(HB).where(HB_ID.eq(id)).execute() > 0;
    }

    @Override
    public long getNextId() throws SQLException {
        Result<Record1<Long>> result = dsl.select(field("nextval('human_beings_id_seq')", Long.class))
                .fetch();
        if (result.isEmpty()) {
            throw new SQLException("Не удалось получить следующий ID из последовательности");
        }
        return result.get(0).get(0, Long.class);
    }

    @Override
    @Transactional
    public void saveAll(List<HumanBeing> collection) throws SQLException {
        dsl.execute("DELETE FROM human_beings");
        if (collection.isEmpty()) return;

        BatchBindStep batch = dsl.batch(
                dsl.insertInto(HB)
                        .columns(HB_ID, HB_NAME, HB_X, HB_Y, HB_CREATION, HB_REAL_HERO,
                                HB_TOOTHPICK, HB_SPEED, HB_WEAPON, HB_MOOD, HB_CAR, HB_OWNER)
        );
        for (HumanBeing h : collection) {
            batch.bind(h.getId(), h.getName(),
                    h.getCoordinates().getX(), h.getCoordinates().getY(),
                    Timestamp.from(h.getCreationDate().toInstant()),
                    h.getRealHero(),
                    h.isHasToothpick(), h.getImpactSpeed(),
                    h.getWeaponType().name(), h.getMood().name(),
                    h.getCar() != null ? h.getCar().getName() : null,
                    h.getOwner().getLogin());
        }
        batch.execute();
    }

    private void validate(HumanBeing h) throws RepositoryException {
        if (h == null || h.getName() == null || h.getCoordinates() == null ||
                h.getRealHero() == null || h.getImpactSpeed() == null ||
                h.getWeaponType() == null || h.getMood() == null || h.getOwner() == null) {
            throw new RepositoryException("HumanBeing содержит обязательные null-поля");
        }
    }

    private HumanBeing mapRecord(org.jooq.Record r) {
        String carName = r.get(HB_CAR);

        //Читаем как OffsetDateTime (так возвращает PostgreSQL драйвер)
        java.time.OffsetDateTime odt = r.get(HB_CREATION);
        ZonedDateTime creationDate = odt != null ?
                odt.atZoneSameInstant(java.time.ZoneId.systemDefault()) :
                ZonedDateTime.now();

        return new HumanBeing(
                r.get(HB_ID),
                r.get(HB_NAME),
                new Coordinates(r.get(HB_X), r.get(HB_Y)),
                creationDate,
                r.get(HB_REAL_HERO), r.get(HB_TOOTHPICK),
                r.get(HB_SPEED),
                WeaponType.valueOf(r.get(HB_WEAPON)),
                Mood.valueOf(r.get(HB_MOOD)),
                carName != null && !carName.isEmpty() ? new Car(carName) : null,
                new User(r.get(U_LOGIN), r.get(U_PASS))
        );
    }
}