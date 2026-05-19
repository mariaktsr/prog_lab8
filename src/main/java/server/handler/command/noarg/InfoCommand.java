package server.handler.command.noarg;

import common.request.Request;
import common.response.Response;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда вывода информации о коллекции

public class InfoCommand implements Command {

    private final ICollectionManager manager;

    public InfoCommand(ICollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Response execute(Request request) {
        String info = manager.getInfo();
        return Response.success(info);
    }

    @Override
    public String getDescription() {
        return "вывести информацию о коллекции (тип, дата инициализации, количество элементов)";
    }
}