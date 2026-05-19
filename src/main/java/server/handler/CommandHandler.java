package server.handler;

import common.commands.CommandType;
import common.model.CommandDescriptor;
import common.model.User;
import common.request.Request;
import common.response.Response;
import server.db.DatabaseManager;
import server.handler.command.CommandInvoker;

import java.io.Serializable;
import java.util.Map;

//Обработчик команд на сервере
//Маршрутизирует запросы, проверяет аутентификацию и делегирует выполнение в CommandInvoker

public class CommandHandler {

    private final CommandInvoker invoker;
    private final CommandRegistry registry;
    private final AuthManager authManager;
    private final CollectionManager collectionManager;

    public CommandHandler(CollectionManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.authManager = new AuthManager(dbManager);
        this.registry = new CommandRegistry(collectionManager, dbManager);
        this.invoker = registry.createInvoker();
    }

    public Response handle(Request request, boolean isFromServerConsole) {
        CommandType type = request.getCommandType();

        if (type == CommandType.GET_COMMANDS_METADATA) {
            Map<String, CommandDescriptor> clientCommands = registry.getClientDescriptors();
            return Response.success("Метаданы синхронизированы", (Serializable) clientCommands);
        }

        if (!isFromServerConsole) {
            if (type == CommandType.LOGIN) {
                return handleLogin(request);
            }
            if (type == CommandType.REGISTER) {
                return handleRegister(request);
            }

            User user = request.getUser();
            if (user == null || !authManager.verifyUser(user)) {
                return Response.error("Ошибка аутентификации. Выполните команду 'login' или 'register'.");
            }
        }

        if (registry.isServerOnly(type) && !isFromServerConsole) {
            return Response.error("Команда '" + type.name().toLowerCase() + "' доступна только на сервере");
        }

        return invoker.execute(request);
    }

    private Response handleLogin(Request request) {
        User user = request.getUser();
        if (user == null) return Response.error("Не переданы учетные данные для входа");
        User authenticated = authManager.authenticate(user.getLogin(), user.getPasswordHash());
        return authenticated != null ?
                Response.success("Вход выполнен успешно: " + authenticated.getLogin(), (Serializable) authenticated) :
                Response.error("Неверный логин или пароль");
    }

    private Response handleRegister(Request request) {
        User user = request.getUser();
        if (user == null) return Response.error("Не переданы учетные данные для регистрации");
        boolean success = authManager.register(user.getLogin(), user.getPasswordHash());
        return success ?
                Response.success("Регистрация успешна: " + user.getLogin()) :
                Response.error("Пользователь с таким логином уже существует");
    }
}