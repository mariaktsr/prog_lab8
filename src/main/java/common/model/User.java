package common.model;

import java.io.Serializable;
import java.util.Objects;

//Класс пользователя для системы аутентификации
//(cодержит логин и хэш пароля (SHA-384))

public class User implements Serializable {

    private final String login;
    private final String passwordHash; //SHA-384 hash

    public User(String login, String passwordHash) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Хэш пароля не может быть пустым");
        }
        this.login = login.trim();
        this.passwordHash = passwordHash;
    }

    public String getLogin() {
        return login;
    }
    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }
    @Override
    public int hashCode() {
        return Objects.hash(login);
    }
    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}