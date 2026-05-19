package common.commands;

import java.io.Serializable;

//Перечисление типов команд для обмена между клиентом и сервером
//(все команды сериализуются и передаются по сети)

public enum CommandType implements Serializable {

    //Команды без аргументов
    CLEAR,
    EXIT,
    HELP,
    INFO,
    PRINT_FIELD_DESCENDING_MOOD,
    SHOW,
    SORT,
    SUM_OF_IMPACT_SPEED,

    //Команды с одним аргументом
    EXECUTE_SCRIPT,
    FILTER_CONTAINS_NAME,
    REMOVE_AT,
    REMOVE_BY_ID,

    //Команды со сложными данными
    ADD,
    INSERT_AT,
    UPDATE,

    //Команды аутентификации
    LOGIN,
    REGISTER,

    GET_COMMANDS_METADATA,

    //Команды, доступные ТОЛЬКО серверу
    SAVE_SERVER;

    public boolean requiresData() {
        return this == ADD || this == INSERT_AT || this == UPDATE;
    }
    public boolean isServerOnly() {
        return this == SAVE_SERVER;
    }
    public boolean requiresArguments() {
        return this == EXECUTE_SCRIPT || this == FILTER_CONTAINS_NAME ||
                this == REMOVE_AT || this == REMOVE_BY_ID ||
                this == INSERT_AT || this == UPDATE;
    }
}