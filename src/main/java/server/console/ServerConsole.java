package server.console;

import common.commands.CommandType;
import common.request.Request;
import common.response.Response;
import server.handler.CommandHandler;

import java.util.Scanner;

//Консоль сервера для административных задач
//(поддерживает только команду save, exit)

public class ServerConsole implements Runnable {

    private final CommandHandler commandHandler;
    private volatile boolean running = true;

    public ServerConsole(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("-СЕРВЕРНАЯ КОНСОЛЬ-");
        System.out.println("  Введите 'save' для сохранения");
        System.out.println("  Введите 'exit' для выхода");

        while (running) {
            try {
                System.out.print("\nserver> ");

                if (!scanner.hasNextLine()) {
                    System.out.println("\nВвод завершён. Завершение сервера...");
                    stop();
                    System.exit(0);
                    return;
                }

                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Завершение работы сервера...");
                    stop();
                    System.exit(0);
                    return;
                }

                if (line.equalsIgnoreCase("save")) {
                    Request request = new Request(CommandType.SAVE_SERVER, new String[0]);
                    Response response = commandHandler.handle(request, true);

                    if (response.isSuccess()) {
                        System.out.println("Успех: " + response.getMessage());
                    } else {
                        System.err.println("Ошибка: " + response.getMessage());
                    }
                } else {
                    System.err.println("Неизвестная команда: " + line);
                    System.err.println("Доступно: save, exit");
                }
            } catch (java.util.NoSuchElementException e) {
                System.out.println("\nВвод завершён. Завершение сервера...");
                stop();
                System.exit(0);
                return;
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}