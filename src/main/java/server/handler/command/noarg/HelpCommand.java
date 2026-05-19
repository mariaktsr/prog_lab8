package server.handler.command.noarg;

import common.commands.CommandType;
import common.request.Request;
import common.response.Response;
import server.handler.command.Command;

import java.util.Map;
import java.util.function.Supplier;

//Команда вывода справки по доступным командам

public class HelpCommand implements Command {

    private final Supplier<Map<CommandType, Command>> commandsProvider;

    public HelpCommand(Supplier<Map<CommandType, Command>> commandsProvider) {
        this.commandsProvider = commandsProvider;
    }

    @Override
    public Response execute(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Доступные команды:\n");
        sb.append("-------------------------------------------------------------------------------\n");

        Map<CommandType, Command> commands = commandsProvider.get();
        for (Map.Entry<CommandType, Command> entry : commands.entrySet()) {
            if (!entry.getKey().isServerOnly()) {
                sb.append(String.format("  %-30s : %s\n",
                        entry.getKey().name().toLowerCase(),
                        entry.getValue().getDescription()));
            }
        }

        sb.append("-------------------------------------------------------------------------------\n");
        return Response.success(sb.toString());
    }

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }
}