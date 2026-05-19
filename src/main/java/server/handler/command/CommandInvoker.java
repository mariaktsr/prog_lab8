package server.handler.command;

import common.commands.CommandType;
import common.request.Request;
import common.response.Response;

import java.util.HashMap;
import java.util.Map;

//Инвокер команд (хранит зарегистрированные команды и выполняет их)

public class CommandInvoker {

    private final Map<CommandType, Command> commands;

    public CommandInvoker() {
        this.commands = new HashMap<>();
    }

    public void register(CommandType type, Command command) {
        commands.put(type, command);
    }

    public Response execute(Request request) {
        CommandType type = request.getCommandType();
        Command command = commands.get(type);

        if (command == null) {
            return Response.error("Неизвестная команда: " + type);
        }

        try {
            return command.execute(request);
        } catch (Exception e) {
            return Response.error("Ошибка выполнения команды: " + e.getMessage());
        }
    }
    public Map<CommandType, Command> getCommands() {
        return commands;
    }
}