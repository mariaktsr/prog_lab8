package server.handler.command.noarg;

import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.ICollectionManager;
import server.handler.command.Command;

import java.util.List;
import java.util.stream.Collectors;

//Команда очистки коллекции

public class ClearCommand implements Command {

    private final ICollectionManager manager;
        private final DatabaseManager dbManager;

        public ClearCommand(ICollectionManager manager, DatabaseManager dbManager) {
            this.manager = manager;
            this.dbManager = dbManager;
        }

        @Override
        public Response execute(Request request) {
            User user = request.getUser();

            if (user == null) {
                return Response.error("Ошибка: пользователь не аутентифицирован");
            }

            try {
                // 1. Находим все элементы, принадлежащие текущему пользователю
                List<HumanBeing> userElements = manager.getCollection().stream()
                        .filter(h -> h.getOwner().getLogin().equals(user.getLogin()))
                        .collect(Collectors.toList());

                if (userElements.isEmpty()) {
                    return Response.success("Коллекция пуста (у вас нет элементов)");
                }

                // 2. Удаляем из БД (Сначала база!)
                boolean allDeleted = true;
                for (HumanBeing human : userElements) {
                    boolean deleted = dbManager.deleteById(human.getId());
                    if (!deleted) {
                        allDeleted = false;
                        // Если удалилось не всё, можно прервать или продолжить.
                        // Для лабы лучше продолжить, но знать о проблеме.
                    }
                }

                // 3. Удаляем из Памяти (Только после попытки удалить из БД)
                manager.getCollection().removeIf(h -> h.getOwner().getLogin().equals(user.getLogin()));

                return Response.success("Коллекция очищена");

            } catch (Exception e) {
                return Response.error("Ошибка выполнения команды: " + e.getMessage());
            }
        }

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }
}