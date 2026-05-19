package common.request;

import common.commands.CommandType;
import common.model.User;

import java.io.Serializable;

//Запрос от клиента к серверу

public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CommandType commandType;
    private final String[] arguments;
    private final Serializable data;
    private final User user;

    //Конструктор для команд без данных и пользователя (обратная совместимость)
    public Request(CommandType commandType, String[] arguments) {
        this(commandType, arguments, null, null);
    }

    //Конструктор для команд с данными, но без пользователя
    public Request(CommandType commandType, String[] arguments, Serializable data) {
        this(commandType, arguments, data, null);
    }

    //Полный конструктор с аутентификацией
    public Request(CommandType commandType, String[] arguments, Serializable data, User user) {
        if (commandType == null) {
            throw new IllegalArgumentException("Тип команды не может быть null");
        }
        this.commandType = commandType;
        if (arguments != null) {
            this.arguments = arguments;
        } else {
            this.arguments = new String[0];
        }
        this.data = data;
        this.user = user;
    }

    public CommandType getCommandType() {
        return commandType;
    }
    public String[] getArguments() {
        return arguments;
    }
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            return (T) data;
        }
        return null;
    }
    public User getUser() {
        return user;
    }

    public boolean hasData() {
        return data != null;
    }

    @Override
    public String toString() {
        return "Request{commandType=" + commandType +
                ", args=" + arguments.length +
                ", hasData=" + (data != null) +
                ", user=" + (user != null ? user.getLogin() : "null") +
                '}';
    }
}