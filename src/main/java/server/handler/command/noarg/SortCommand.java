package server.handler.command.noarg;

import common.request.Request;
import common.response.Response;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда сортировки коллекции

public class SortCommand implements Command {
    private final ICollectionManager manager;

    public SortCommand(ICollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Response execute(Request request) {
        return Response.success(manager.getSortedCollection());
    }

    @Override
    public String getDescription() {
        return "отсортировать коллекцию по полю name";
    }
}