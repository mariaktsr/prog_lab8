package server.handler.command.noarg;

import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда сохранения коллекции в файл
//(доступна ТОЛЬКО на сервере)

public class SaveServerCommand implements Command {

    private final ICollectionManager manager;
    private final DatabaseManager dbManager;

    public SaveServerCommand(ICollectionManager manager, DatabaseManager dbManager) {
        this.manager = manager;
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(Request request) {
        try {
            dbManager.saveAllToDatabase(manager.getCollection());
            return Response.success("Коллекция сохранена в БД");
        } catch (Exception e) {
            return Response.error("Ошибка при сохранении: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию в базу данных (только для сервера)";
    }
}