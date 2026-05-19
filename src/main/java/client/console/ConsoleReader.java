package client.console;

import client.network.NetworkClient;
import client.validation.DynamicValidationFactory;
import client.validation.Validation;
import common.commands.CommandType;
import common.model.CommandDescriptor;
import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import common.util.PasswordHasher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

//Чтение команд из консоли, управление сессией пользователя и формирование запросов
//Содержит поле currentUser, которое прикрепляется к каждому запросу

public class ConsoleReader {

    private final Scanner scanner;
    private final InputHelper inputHelper;
    private final DynamicValidationFactory validationFactory;
    private final NetworkClient networkClient;

    private Map<String, CommandDescriptor> commandDescriptors = new HashMap<>();

    //Хранит текущего авторизованного пользователя
    private User currentUser = null;

    public ConsoleReader(Scanner scanner, NetworkClient networkClient) {
        this.scanner = scanner;
        this.networkClient = networkClient;
        this.inputHelper = new InputHelper(scanner);
        this.validationFactory = new DynamicValidationFactory();
    }

    //Запрашивает у сервера метаданные команд (рукопожатие)
    //Вызывается один раз сразу после подключения
    public boolean fetchCommandMetadata(NetworkClient networkClient) {
        try {
            //Используем специальную команду для запроса метаданных
            Request bootstrap = new Request(CommandType.GET_COMMANDS_METADATA, new String[0]);
            Response response = networkClient.sendRequest(bootstrap);

            if (response.isSuccess() && response.getData(Map.class) != null) {
                @SuppressWarnings("unchecked")
                Map<String, CommandDescriptor> received = (Map<String, CommandDescriptor>) response.getData(Map.class);
                this.commandDescriptors = received;
                System.out.println("Синхронизация команд завершена. Доступно: " + commandDescriptors.size());
                return true;
            } else {
                System.err.println("Не удалось получить метаданные команд: " + response.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Ошибка рукопожатия: " + e.getMessage());
            return false;
        }
    }

    public Request readCommand() {
        System.out.print("\n> ");

        if (!scanner.hasNextLine()) {
            System.out.println("\nВвод завершён. Завершение работы...");
            return new Request(CommandType.EXIT, new String[0]);
        }

        String line = scanner.nextLine().trim();

        if (line.isEmpty()) {
            return null;
        }

        String[] tokens = line.split("\\s+", 2);
        String cmdName = tokens[0].toLowerCase();

        CommandType type;
        try {
            type = CommandType.valueOf(cmdName.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Неизвестная команда: " + cmdName);
            return null;
        }

        if (type == CommandType.EXIT) {
            return new Request(type, new String[0]);
        }

        //Обработка команд авторизации
        if (type == CommandType.LOGIN || type == CommandType.REGISTER) {
            handleAuthCommand(type, tokens);
            return null; //Запрос уже отправлен внутри метода
        }

        //Запрет выполнения команд без авторизации
        if (currentUser == null) {
            System.err.println("Ошибка: сначала выполните команду 'login' или 'register'.");
            return null;
        }

        //Дескриптор и валидация аргументов
        CommandDescriptor descriptor = commandDescriptors.get(cmdName);
        String[] args = tokens.length > 1 ? new String[]{tokens[1]} : new String[0];

        if (descriptor != null && !descriptor.isServerOnly()) {
            Validation<String[]> validator = validationFactory.createValidator(descriptor);
            Validation.ValidationError error = validator.validate(args);
            if (error != null) {
                System.err.println(error.getMessage());
                return null;
            }
        } else if (descriptor != null && descriptor.isServerOnly()) {
            System.err.println("Команда '" + cmdName + "' доступна только на сервере");
            return null;
        }

        //Формирование запроса
        if (descriptor != null && descriptor.isRequiresData()) {
            return readComplexCommand(type, args, currentUser);
        }

        //Прикрепляем пользователя ко ВСЕМ запросам
        return new Request(type, args, null, currentUser);
    }

    //Интерактивная обработка login/register
    private void handleAuthCommand(CommandType type, String[] tokens) {
        System.out.print(" Введите логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (login.isEmpty() || password.isEmpty()) {
            System.err.println("Логин и пароль не могут быть пустыми");
            return;
        }

        String hash = PasswordHasher.hash(password);
        User authUser = new User(login, hash);
        Request request = new Request(type, new String[0], null, authUser);

        try {
            Response response = networkClient.sendRequest(request);
            if (response.isSuccess()) {
                System.out.println("Успех: " + response.getMessage());
                if (type == CommandType.REGISTER) {
                    currentUser = authUser; //При регистрации сразу становимся пользователем
                    System.out.println("Добро пожаловать, " + currentUser.getLogin() + "!");
                } else if (type == CommandType.LOGIN && response.getData(User.class) != null) {
                    currentUser = response.getData(User.class);
                    System.out.println("Добро пожаловать, " + currentUser.getLogin() + "!");
                }
            } else {
                System.err.println("Ошибка: " + response.getMessage());
                currentUser = null;
            }
        } catch (IOException e) {
            System.err.println("Ошибка связи с сервером: " + e.getMessage());
        }
    }

    //Чтение сложных команд (add, update, insert_at) с прикреплённым пользователем
    private Request readComplexCommand(CommandType type, String[] args, User user) {
        try {
            Long existingId = null;
            if (type == CommandType.UPDATE && args.length > 0) {
                existingId = Long.parseLong(args[0].trim());
            } else if (type == CommandType.INSERT_AT && args.length > 0) {
                int index = Integer.parseInt(args[0].trim());
                if (index < 0) {
                    System.err.println("Индекс не может быть отрицательным");
                    return null;
                }
                args = new String[]{String.valueOf(index)};
            }

            System.out.println("\nВвод данных элемента:");
            //Передаём текущего пользователя в InputHelper, чтобы он установил owner в объекте
            HumanBeing human = inputHelper.readHumanBeing(existingId, user);
            return new Request(type, args, human, user);

        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга аргумента: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка ввода: " + e.getMessage());
            return null;
        }
    }

    public InputHelper getInputHelper() {
        return inputHelper;
    }

    public Map<String, CommandDescriptor> getCommandDescriptors() {
        return commandDescriptors;
    }

    public User getCurrentUser() { return currentUser; }
}