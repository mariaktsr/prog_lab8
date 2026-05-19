package server.handler.command.complex;

import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.CollectionManager;
import server.handler.CollectionManagerProxy;
import server.handler.ICollectionManager;
import server.handler.command.Command;

//Команда вставки элемента в заданную позицию

public class InsertAtCommand implements Command {

    private final ICollectionManager manager;
    private final DatabaseManager dbManager;

    public InsertAtCommand(ICollectionManager manager, DatabaseManager dbManager) {
        this.manager = manager;
        this.dbManager = dbManager;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArguments();
        HumanBeing human = request.getData(HumanBeing.class);
        User user = request.getUser();

        if (args == null || args.length == 0) return Response.error("Команда insert_at требует индекс!");
        if (human == null) return Response.error("Внутренняя ошибка: объект не передан");
        if (user == null) return Response.error("Пользователь не аутентифицирован");

        int index;
        try {
            index = Integer.parseInt(args[0].trim());
        } catch (NumberFormatException e) {
            return Response.error("Индекс должен быть целым числом!");
        }

        try {
            human.setOwner(user);
            human.setCreationDate(java.time.ZonedDateTime.now());

            //здесь предполагаем, что мы сначала добавляем в память (с генерацией ID),
            //а потом синхронизируем или вставляем в БД как новый объект

            //генерируем ID через БД
            long newId = dbManager.getNextIdFromSequence();
            human.setId(newId);

            //сохраняем в БД
            boolean saved = dbManager.save(human);

            //вставляем в память через ПРОКСИ
            if (saved) {
                CollectionManager realManager = (CollectionManager) manager;
                CollectionManagerProxy proxy = new CollectionManagerProxy(realManager, user.getLogin());

                //Proxy просто пропустит этот вызов, так как элемент новый (проверка прав не блокирует создание)
                proxy.insertAt(index, human);
                return Response.success("Элемент успешно вставлен по индексу " + index + " с ID: " + newId);
            } else {
                return Response.error("Ошибка сохранения в базу данных");
            }
        } catch (Exception e) {
            return Response.error("Ошибка выполнения команды: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в заданную позицию";
    }
}