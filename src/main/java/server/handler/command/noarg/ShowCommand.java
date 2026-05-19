package server.handler.command.noarg;

import common.model.HumanBeing;
import common.request.Request;
import common.response.Response;
import server.handler.ICollectionManager;
import server.handler.command.Command;

import java.io.Serializable;
import java.util.List;

//Команда вывода всех элементов коллекции
public class ShowCommand implements Command {

    private final ICollectionManager manager;

    public ShowCommand(ICollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public Response execute(Request request) {
        // Получаем коллекцию как список объектов (не как строку!)
        List<HumanBeing> collection = manager.getCollection();

        if (collection == null || collection.isEmpty()) {
            return Response.success("Коллекция пуста");
        }

        // Отправляем список в поле DATA, чтобы JavaFX мог десериализовать объекты
        return Response.success("Коллекция получена: " + collection.size() + " элементов",
                (Serializable) collection);
    }

    @Override
    public String getDescription() {
        return "вывести все элементы коллекции в строковом представлении";
    }
}