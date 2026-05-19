package server.repository.legacy;

import server.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

//Legacy-реализация репозитория пользователей на основе JDBC и PreparedStatement
//Потокобезопасность достигается за счёт использования пула соединений HikariCP
//(каждое обращение к БД получает своё соединение из пула)
public class LegacyUserRepository implements UserRepository {

    private final DataSource dataSource;

    public LegacyUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void initSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                login VARCHAR(255) PRIMARY KEY,
                password_hash VARCHAR(100) NOT NULL
            )
            """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public boolean register(String login, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            //PostgreSQL возвращает SQLState 23505 при нарушении UNIQUE constraint
            if ("23505".equals(e.getSQLState())) {
                return false; //пользователь с таким логином уже существует
            }
            throw e; //пробрасываем другие критические ошибки БД
        }
    }

    @Override
    public Optional<String> findPasswordHashByLogin(String login) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE login = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("password_hash")) : Optional.empty();
            }
        }
    }
}