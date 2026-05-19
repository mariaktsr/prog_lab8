package client.console;

import client.handler.ResponseHandler;
import client.network.NetworkClient;
import client.validation.DynamicValidationFactory;
import client.validation.Validation;
import common.commands.CommandType;
import common.model.CommandDescriptor;
import common.model.HumanBeing;
import common.request.Request;
import common.response.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

//Выполняет скрипты из файлов, используя динамические метаданные от сервера

public class ScriptExecutor {

    private final ConsoleReader consoleReader;
    private final NetworkClient networkClient;
    private final ResponseHandler responseHandler;
    private final DynamicValidationFactory validationFactory;

    //Защита от рекурсивных вызовов
    private final Deque<String> scriptStack = new ArrayDeque<>();
    private static final int MAX_SCRIPT_DEPTH = 5;

    public ScriptExecutor(ConsoleReader consoleReader, NetworkClient networkClient, ResponseHandler responseHandler) {
        this.consoleReader = consoleReader;
        this.networkClient = networkClient;
        this.responseHandler = responseHandler;
        this.validationFactory = new DynamicValidationFactory();
    }

    //Выполняет скрипт из файла
    public boolean executeScript(String fileName) {

        if (scriptStack.contains(fileName)) {
            System.err.println("Обнаружена рекурсивная ссылка на скрипт: " + fileName);
            return false;
        }

        if (scriptStack.size() >= MAX_SCRIPT_DEPTH) {
            System.err.println("Превышена максимальная глубина вложенности скриптов (" + MAX_SCRIPT_DEPTH + ")");
            return false;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("Файл не найден: " + fileName);
            return false;
        }

        if (!file.canRead()) {
            System.err.println("Нет прав на чтение файла: " + fileName);
            return false;
        }

        scriptStack.push(fileName);
        System.out.println("Начало выполнения скрипта: " + fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                System.out.println("[" + lineNumber + "] " + line);

                executeCommand(line);
            }

            System.out.println("Завершение выполнения скрипта: " + fileName);
            return true;

        } catch (IOException e) {
            System.err.println("Ошибка чтения скрипта: " + e.getMessage());
            return false;
        } finally {
            scriptStack.pop();
        }
    }

    //Выполняет одну команду из скрипта с использованием динамических метаданных
    private void executeCommand(String line) {
        String[] tokens = line.split("\\s+", 2);
        String cmdName = tokens[0].toLowerCase();

        //получаем дескриптор от сервера
        CommandDescriptor desc = consoleReader.getCommandDescriptors().get(cmdName);
        if (desc == null) {
            System.err.println("Неизвестная команда в скрипте: " + cmdName);
            return;
        }

        //проверки через дескриптор
        if (desc.isServerOnly()) {
            System.err.println("Команда '" + cmdName + "' доступна только на сервере, пропускается");
            return;
        }

        if (cmdName.equals("exit")) {
            System.out.println("Команда 'exit' в скрипте игнорируется");
            return;
        }

        if (cmdName.equals("execute_script")) {
            if (tokens.length > 1) {
                executeScript(tokens[1].trim());
            } else {
                System.err.println("execute_script требует имя файла");
            }
            return;
        }

        //проверка авторизации перед выполнением команд от имени пользователя
        if (consoleReader.getCurrentUser() == null) {
            System.err.println("Скрипт требует авторизации. Выполните 'login' перед запуском скрипта.");
            return;
        }

        String[] args = tokens.length > 1 ? new String[]{tokens[1]} : new String[0];

        Validation<String[]> validator = validationFactory.createValidator(desc);
        Validation.ValidationError error = validator.validate(args);
        if (error != null) {
            System.err.println("Ошибка валидации в скрипте [" + cmdName + "]: " + error.getMessage());
            return;
        }

        //преобразование в CommandType (для совместимости с текущим Request)
        CommandType type;
        try {
            type = CommandType.valueOf(cmdName.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Команда '" + cmdName + "' отсутствует в клиентском enum.");
            return;
        }

        //выполнение
        try {
            if (desc.isRequiresData()) {
                Long existingId = null;
                if (type == CommandType.UPDATE && args.length > 0) {
                    existingId = Long.parseLong(args[0].trim());
                } else if (type == CommandType.INSERT_AT && args.length > 0) {
                    int index = Integer.parseInt(args[0].trim());
                    if (index < 0) {
                        System.err.println("Индекс не может быть отрицательным");
                        return;
                    }
                    args = new String[]{String.valueOf(index)};
                }

                System.out.println("  [Скрипт] Ввод данных элемента:");
                //передаём текущего авторизованного пользователя
                HumanBeing human = consoleReader.getInputHelper().readHumanBeing(existingId, consoleReader.getCurrentUser());
                sendAndHandle(new Request(type, args, human, consoleReader.getCurrentUser()));
            } else {
                sendAndHandle(new Request(type, args, null, consoleReader.getCurrentUser()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга аргумента в скрипте: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка выполнения команды '" + cmdName + "': " + e.getMessage());
        }
    }

    // Вспомогательный метод для отправки и обработки ответа
    private void sendAndHandle(Request request) {
        try {
            Response response = networkClient.sendRequest(request);
            responseHandler.handle(response);
        } catch (IOException e) {
            System.err.println("Ошибка связи с сервером при выполнении скрипта: " + e.getMessage());
        }
    }
}