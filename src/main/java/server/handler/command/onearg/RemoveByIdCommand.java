package server.handler.command.onearg;

import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.CollectionManager;
import server.handler.CollectionManagerProxy;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда удаления элемента по ID.

public class RemoveByIdCommand implements Command {

    private final ICollectionManager manager;
    private final DatabaseManager dbManager;

    public RemoveByIdCommand(ICollectionManager manager, DatabaseManager dbManager) {
        this.manager = manager;
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArguments();
        User user = request.getUser();

        if (args == null || args.length < 1) {
            return Response.error("Команда требует ID");
        }
        if (user == null) {
            return Response.error("Пользователь не аутентифицирован");
        }

        Long id;
        try {
            id = Long.parseLong(args[0].trim());
        } catch (NumberFormatException e) {
            return Response.error("ID должен быть числом");
        }

        try {
            // === 1. Сначала проверяем права через ПРОКСИ (до БД!) ===
            CollectionManager realManager = (CollectionManager) manager;
            CollectionManagerProxy proxy = new CollectionManagerProxy(realManager, user.getLogin());

            // Проверяем, принадлежит ли элемент пользователю
            // Если нет — прокси бросит SecurityException ДО обращения к БД
            if (!proxy.isOwner(id, user.getLogin())) {
                return Response.error("Доступ запрещен: объект с ID " + id + " принадлежит другому пользователю.");
            }

            // === 2. Только если права есть — удаляем из БД ===
            boolean deletedFromDb = dbManager.deleteById(id);

            // === 3. Если БД успешно — удаляем из памяти ===
            if (deletedFromDb) {
                proxy.removeById(id); // Теперь это безопасно
                return Response.success("Элемент с ID " + id + " удалён");
            } else {
                return Response.error("Элемент с ID " + id + " не найден в базе данных");
            }
        } catch (SecurityException e) {
            return Response.error(e.getMessage());
        } catch (Exception e) {
            return Response.error("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удалить элемент из коллекции по его id";
    }
}