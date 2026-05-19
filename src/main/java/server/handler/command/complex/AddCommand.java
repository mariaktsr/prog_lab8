package server.handler.command.complex;

import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.ICollectionManager;
import server.handler.command.Command;

import java.time.ZonedDateTime;

//Команда добавления нового элемента в коллекцию

public class AddCommand implements Command {

    private final ICollectionManager manager;
    private final DatabaseManager dbManager;

    public AddCommand(ICollectionManager manager, DatabaseManager dbManager) {
        this.manager = manager;
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(Request request) {
        HumanBeing human = request.getData(HumanBeing.class);
        User user = request.getUser();

        if (human == null) {
            return Response.error("Внутренняя ошибка: объект не передан");
        }
        if (user == null) {
            return Response.error("Пользователь не аутентифицирован");
        }

        try {
            //устанавливаем владельца и дату создания (делает сервер)
            human.setOwner(user);
            human.setCreationDate(ZonedDateTime.now());

            //сохраняем в БД
            boolean saved = dbManager.save(human);

            //если в БД успешно -> обновляем ПАМЯТЬ
            if (saved) {
                manager.add(human);
                return Response.success("Элемент успешно добавлен с ID: " + human.getId());
            } else {
                return Response.error("Ошибка сохранения в базу данных");
            }
        } catch (Exception e) {
            return Response.error("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию";
    }
}