package server.repository;

import java.sql.SQLException;
import java.util.Optional;

public interface UserRepository {

    void initSchema() throws SQLException;
    boolean register(String login, String passwordHash) throws SQLException;
    Optional<String> findPasswordHashByLogin(String login) throws SQLException;

}