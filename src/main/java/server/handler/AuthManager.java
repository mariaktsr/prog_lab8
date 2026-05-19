package server.handler;

import common.model.User;
import common.util.PasswordHasher;
import server.db.DatabaseManager;

import java.sql.SQLException;

//Менеджер аутентификации и регистрации пользователей.
public class AuthManager {

    private final DatabaseManager dbManager;

    public AuthManager(DatabaseManager dbManager) {
        if (dbManager == null) {
            throw new IllegalArgumentException("DatabaseManager не может быть null");
        }
        this.dbManager = dbManager;
    }

    //Регистрирует нового пользователя
    public boolean register(String login, String rawPassword) {
        if (login == null || login.trim().isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        try {
            String hash = PasswordHasher.hash(rawPassword.trim());
            return dbManager.register(login.trim(), hash);
        } catch (SQLException e) {
            System.err.println("Ошибка СУБД при регистрации: " + e.getMessage());
            return false;
        }
    }

    //Аутентифицирует пользователя
    public User authenticate(String login, String rawPassword) {
        if (login == null || rawPassword == null) {
            return null;
        }
        try {
            String hash = PasswordHasher.hash(rawPassword.trim());
            String authenticatedLogin = dbManager.authenticate(login.trim(), hash);
            return authenticatedLogin != null ? new User(authenticatedLogin, hash) : null;
        } catch (SQLException e) {
            System.err.println("Ошибка СУБД при аутентификации: " + e.getMessage());
            return null;
        }
    }

    //Проверяет валидность учетных данных, переданных в запросе.
    //Вызывается при обработке КАЖДОЙ команды (кроме login/register).
    public boolean verifyUser(User user) {
        if (user == null) {
            return false;
        }
        try {
            //сравниваем переданный хэш с хэшем из БД без повторного хэширования
            return dbManager.authenticate(user.getLogin(), user.getPasswordHash()) != null;
        } catch (SQLException e) {
            System.err.println("Ошибка СУБД при верификации: " + e.getMessage());
            return false;
        }
    }
}