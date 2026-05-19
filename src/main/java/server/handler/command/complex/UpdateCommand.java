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

//Команда обновления элемента по ID

    public class UpdateCommand implements Command {

        private final ICollectionManager manager;
        private final DatabaseManager dbManager;

        public UpdateCommand(ICollectionManager manager, DatabaseManager dbManager) {
            this.manager = manager;
            this.dbManager = dbManager;
        }

        @Override
        public Response execute(Request request) {
            HumanBeing updatedHuman = request.getData(HumanBeing.class);
            User user = request.getUser();

            if (updatedHuman == null) {
                return Response.error("Ошибка: объект не передан");
            }
            if (user == null) {
                return Response.error("Ошибка: пользователь не аутентифицирован");
            }

            Long id = updatedHuman.getId();
            if (id == null) {
                return Response.error("Ошибка: ID не может быть null");
            }

            try {
                // 1. ПРОВЕРКА ПРАВ ДО БД
                // Создаем прокси для проверки владения
                CollectionManagerProxy proxy = new CollectionManagerProxy((CollectionManager) manager, user.getLogin());

                // Если метод isOwner не существует в вашем Proxy, используйте логику проверки вручную:
                // boolean isOwner = manager.getCollection().stream()
                //    .anyMatch(h -> h.getId().equals(id) && h.getOwner().getLogin().equals(user.getLogin()));

                // Для надежности используем поиск в коллекции:
                HumanBeing existingHuman = null;
                for (HumanBeing h : manager.getCollection()) {
                    if (h.getId().equals(id)) {
                        existingHuman = h;
                        break;
                    }
                }

                if (existingHuman == null) {
                    return Response.error("Ошибка: элемент с ID " + id + " не найден в коллекции");
                }

                if (!existingHuman.getOwner().getLogin().equals(user.getLogin())) {
                    return Response.error("Доступ запрещен: элемент с ID " + id + " принадлежит другому пользователю");
                }

                // Сохраняем важные поля, которые нельзя перезаписывать данными от клиента
                updatedHuman.setCreationDate(existingHuman.getCreationDate());
                updatedHuman.setOwner(existingHuman.getOwner());
                updatedHuman.setId(existingHuman.getId());

                // 2. ОБНОВЛЕНИЕ БД
                boolean dbResult = dbManager.update(updatedHuman);

                // 3. ОБНОВЛЕНИЕ ПАМЯТИ (только если БД успешна)
                if (dbResult) {
                    proxy.update(id, updatedHuman); // Или manager.update(updatedHuman)
                    return Response.success("Элемент с ID " + id + " успешно обновлен");
                } else {
                    return Response.error("Ошибка: не удалось обновить элемент в базе данных");
                }

            } catch (Exception e) {
                return Response.error("Ошибка выполнения команды: " + e.getMessage());
            }
        }

    @Override
    public String getDescription() {
        return "обновить значение элемента коллекции, id которого равен заданному";
    }
}