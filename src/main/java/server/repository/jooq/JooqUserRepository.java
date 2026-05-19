package server.repository.jooq;

import org.jooq.*;
import org.jooq.exception.DataAccessException;
import server.repository.UserRepository;
import server.repository.jooq.proxy.Transactional;

import java.sql.SQLException;
import java.util.Optional;

import static org.jooq.impl.DSL.*;

//jOOQ-реализация репозитория для управления пользователями
//Обеспечивает типобезопасные запросы для регистрации и аутентификации
//Аналогично JooqHumanBeingRepository, использует DSLContext из HikariCP пула
//и аннотацию @Transactional для декларативного управления транзакциями
public class JooqUserRepository implements UserRepository {

    private final DSLContext dsl;

    private static final Table<?> USERS = table("users");
    private static final Field<String> U_LOGIN = field("login", String.class);
    private static final Field<String> U_PASS = field("password_hash", String.class);

    public JooqUserRepository(DSLContext dsl) {
        if (dsl == null) {
            throw new IllegalArgumentException("DSLContext не может быть null");
        }
        this.dsl = dsl;
    }

    @Override
    public void initSchema() throws SQLException {
        dsl.execute("""
            CREATE TABLE IF NOT EXISTS users (
                login VARCHAR(255) PRIMARY KEY,
                password_hash VARCHAR(100) NOT NULL
            )
            """);
    }

    @Override
    @Transactional //регистрация требует атомарности: либо запись добавлена, либо нет
    public boolean register(String login, String passwordHash) throws SQLException {
        try {
            return dsl.insertInto(USERS)
                    .columns(U_LOGIN, U_PASS)
                    .values(login, passwordHash)
                    .execute() > 0;
        } catch (DataAccessException e) {
            //PostgreSQL генерирует SQLState 23505 при нарушении UNIQUE constraint
            Throwable cause = e.getCause();
            if (cause instanceof SQLException sqlEx && "23505".equals(sqlEx.getSQLState())) {
                return false; //пользователь уже существует
            }
            throw new SQLException("Ошибка регистрации пользователя", e);
        }
    }

    @Override
    public Optional<String> findPasswordHashByLogin(String login) throws SQLException {
        //jOOQ автоматически мапит результат в String или возвращает null
        String hash = dsl.select(U_PASS)
                .from(USERS)
                .where(U_LOGIN.eq(login))
                .fetchOne(U_PASS);
        return Optional.ofNullable(hash);
    }
}