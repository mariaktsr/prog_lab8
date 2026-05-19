package server.handler.command.noarg;

import common.request.Request;
import common.response.Response;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда вывода суммы значений поля impactSpeed

public class SumOfImpactSpeedCommand implements Command {

    private final ICollectionManager manager;

    public SumOfImpactSpeedCommand(ICollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Response execute(Request request) {
        return Response.success(manager.getSumOfImpactSpeed());
    }

    @Override
    public String getDescription() {
        return "вывести сумму значений поля impactSpeed для всех элементов коллекции";
    }
}