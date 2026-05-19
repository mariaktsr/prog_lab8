package server.handler.command.onearg;

import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.CollectionManager;
import server.handler.CollectionManagerProxy;
import server.handler.ICollectionManager;
import server.handler.command.Command;

import java.util.List;

//Команда удаления элемента по индексу

public class RemoveAtCommand implements Command {

    private final ICollectionManager manager;
    private final DatabaseManager dbManager;

    public RemoveAtCommand(ICollectionManager manager, DatabaseManager dbManager) {
        this.manager = manager;
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArguments();
        User user = request.getUser();

        if (args == null || args.length < 1) {
            return Response.error("Команда требует индекс");
        }
        if (user == null) {
            return Response.error("Пользователь не аутентифицирован");
        }

        int index;
        try {
            index = Integer.parseInt(args[0].trim());
        } catch (NumberFormatException e) {
            return Response.error("Индекс должен быть целым числом");
        }

            try {
                List<HumanBeing> collection = manager.getCollection();
                if (index < 0 || index >= collection.size()) {
                    return Response.error("Индекс вне диапазона");
                }

                Long id = collection.get(index).getId();

                // === 1. Проверка прав через прокси (ДО БД) ===
                CollectionManager realManager = (CollectionManager) manager;
                CollectionManagerProxy proxy = new CollectionManagerProxy(realManager, user.getLogin());

                if (!proxy.isOwner(id, user.getLogin())) {
                    return Response.error("Доступ запрещен: объект с индексом " + index + " принадлежит другому пользователю.");
                }

                // === 2. Удаление из БД ===
                boolean deletedFromDb = dbManager.deleteById(id);

                // === 3. Удаление из памяти ===
                if (deletedFromDb) {
                    proxy.removeAt(index);
                    return Response.success("Элемент с индексом " + index + " удалён");
                } else {
                    return Response.error("Ошибка удаления из базы данных");
                }
            } catch (SecurityException e) {
                return Response.error(e.getMessage());
            } catch (Exception e) {
                return Response.error("Ошибка выполнения команды: " + e.getMessage());
            }
        }

    @Override
    public String getDescription() {
        return "удалить элемент, находящийся в заданной позиции коллекции (index)";
    }
}