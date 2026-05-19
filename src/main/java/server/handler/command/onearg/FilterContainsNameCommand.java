package server.handler.command.onearg;

import common.model.HumanBeing;
import common.request.Request;
import common.response.Response;
import server.handler.ICollectionManager;
import server.handler.command.Command;

import java.util.List;
import java.util.stream.Collectors;

//Команда фильтрации элементов по подстроке в имени

public class FilterContainsNameCommand implements Command {

    private final ICollectionManager manager;

    public FilterContainsNameCommand(ICollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArguments();

        if (args == null || args.length < 1) {
            return Response.error("Команда требует подстроку для поиска");
        }

        String substring = args[0];
        List<HumanBeing> result = manager.filterContainsName(substring);

        if (result.isEmpty()) {
            return Response.success("Элементы с именем, содержащим '" + substring + "', не найдены");
        }

        String output = result.stream()
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n"));

        return Response.success(output);
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля name которых содержит заданную подстроку";
    }
}