package server.repository.legacy;

import common.model.*;
import server.repository.base.AbstractHumanBeingRepository;
import server.repository.base.RepositoryException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

//Legacy-реализация репозитория на основе JDBC и PreparedStatement.
//Сохраняет полную обратную совместимость с оригинальной логикой,
//но использует пул соединений HikariCP для оптимизации производительности.
//Потокобезопасность обеспечивается за счёт получения нового соединения из пула
//(при каждом вызове метода (stateless-архитектура))
public class LegacyHumanBeingRepository extends AbstractHumanBeingRepository {

    private final DataSource dataSource;

    public LegacyHumanBeingRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void initSchema() throws SQLException {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                login VARCHAR(255) PRIMARY KEY,
                password_hash VARCHAR(100) NOT NULL
            )
            """;
        String createSequence = """
            CREATE SEQUENCE IF NOT EXISTS human_beings_id_seq
            START WITH 1 INCREMENT BY 1
            """;
        String createTable = """
            CREATE TABLE IF NOT EXISTS human_beings (
                id BIGINT PRIMARY KEY DEFAULT nextval('human_beings_id_seq'),
                name VARCHAR(255) NOT NULL,
                x DOUBLE PRECISION NOT NULL,
                y BIGINT NOT NULL CHECK (y > -228),
                creation_date TIMESTAMPTZ NOT NULL,
                real_hero BOOLEAN NOT NULL,
                has_toothpick BOOLEAN NOT NULL,
                impact_speed BIGINT NOT NULL CHECK (impact_speed > -428),
                weapon_type VARCHAR(50) NOT NULL,
                mood VARCHAR(50) NOT NULL,
                car_name VARCHAR(255),
                owner_login VARCHAR(255) NOT NULL REFERENCES users(login)
            )
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            try {
                stmt.execute(createUsers);
                stmt.execute(createSequence);
                stmt.execute(createTable);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<HumanBeing> findAll() throws SQLException {
        String sql = """
            SELECT hb.*, u.password_hash as owner_password
            FROM human_beings hb
            JOIN users u ON hb.owner_login = u.login
            ORDER BY hb.id
            """;
        List<HumanBeing> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public boolean save(HumanBeing human) throws SQLException {
        try {
            validateHumanBeing(human);
        } catch (RepositoryException e) {
            throw new SQLException("Ошибка валидации при сохранении: " + e.getMessage(), e);
        }

        String sql = """
            INSERT INTO human_beings (id, name, x, y, creation_date, real_hero,
                                      has_toothpick, impact_speed, weapon_type,
                                      mood, car_name, owner_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        //генерируем ID средствами БД перед вставкой
        long newId = getNextId();
        human.setId(newId);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindHumanToStatement(stmt, human, true);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(HumanBeing human) throws SQLException {
        try {
            validateHumanBeing(human);
        } catch (RepositoryException e) {
            throw new SQLException("Ошибка валидации при обновлении: " + e.getMessage(), e);
        }

        String sql = """
            UPDATE human_beings
            SET name=?, x=?, y=?, creation_date=?, real_hero=?, has_toothpick=?,
                impact_speed=?, weapon_type=?, mood=?, car_name=?, owner_login=?
            WHERE id=?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindHumanToStatement(stmt, human, false);
            stmt.setLong(12, human.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM human_beings WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public long getNextId() throws SQLException {
        String sql = "SELECT nextval('human_beings_id_seq')";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
            throw new SQLException("Не удалось получить следующий ID из последовательности");
        }
    }

    @Override
    public void saveAll(List<HumanBeing> collection) throws SQLException {
        String insertSql = """
            INSERT INTO human_beings (id, name, x, y, creation_date, real_hero,
                                      has_toothpick, impact_speed, weapon_type,
                                      mood, car_name, owner_login)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                //очистка таблицы
                try (Statement clearStmt = conn.createStatement()) {
                    clearStmt.execute("DELETE FROM human_beings");
                }

                //пакетная вставка
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (HumanBeing h : collection) {
                        bindHumanToStatement(pstmt, h, true);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    //Вспомогательные методы маппинга
    private HumanBeing mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        double x = rs.getDouble("x");
        Long y = rs.getLong("y");
        Timestamp timestamp = rs.getTimestamp("creation_date");
        ZonedDateTime creationDate = timestamp.toLocalDateTime()
                .atZone(java.time.ZoneId.systemDefault());
        boolean realHero = rs.getBoolean("real_hero");
        boolean hasToothpick = rs.getBoolean("has_toothpick");
        Long impactSpeed = rs.getLong("impact_speed");
        WeaponType weaponType = WeaponType.valueOf(rs.getString("weapon_type"));
        Mood mood = Mood.valueOf(rs.getString("mood"));

        String carName = rs.getString("car_name");
        Car car = (carName != null && !carName.trim().isEmpty()) ? new Car(carName) : null;

        String ownerLogin = rs.getString("owner_login");
        String ownerPassHash = rs.getString("owner_password");
        User owner = new User(ownerLogin, ownerPassHash);

        return new HumanBeing(id, name, new Coordinates(x, y), creationDate,
                realHero, hasToothpick, impactSpeed, weaponType, mood, car, owner);
    }

    private void bindHumanToStatement(PreparedStatement stmt, HumanBeing human,
                                      boolean includeId) throws SQLException {
        int idx = 1;
        if (includeId) {
            stmt.setLong(idx++, human.getId());
        }
        stmt.setString(idx++, human.getName());
        stmt.setDouble(idx++, human.getCoordinates().getX());
        stmt.setLong(idx++, human.getCoordinates().getY());
        stmt.setTimestamp(idx++, Timestamp.from(human.getCreationDate().toInstant()));
        stmt.setBoolean(idx++, human.getRealHero());
        stmt.setBoolean(idx++, human.isHasToothpick());
        stmt.setLong(idx++, human.getImpactSpeed());
        stmt.setString(idx++, human.getWeaponType().name());
        stmt.setString(idx++, human.getMood().name());
        stmt.setString(idx++, human.getCar() != null ? human.getCar().getName() : null);
        stmt.setString(idx, human.getOwner().getLogin());
    }
}