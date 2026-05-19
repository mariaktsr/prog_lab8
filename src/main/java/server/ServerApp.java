package server;

import server.config.RepositoryConfig;
import server.config.ServerConfig;
import server.console.ServerConsole;
import server.db.DatabaseManager;
import server.handler.CollectionManager;
import server.handler.CommandHandler;
import server.network.ConnectionAcceptor;

import java.sql.SQLException;
import java.util.List;

public class ServerApp {

    public static void main(String[] args) {
        System.out.println("SERVER APPLICATION - LAB 7+ (Refactored: Repository + jOOQ + HikariCP)");

        try {
            ServerConfig serverConfig = new ServerConfig();
            RepositoryConfig repoConfig = new RepositoryConfig();

            System.out.println("Конфигурация сервера: порт " + serverConfig.getPort());

            String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
            String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "studs";
            String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
            String dbPass = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "newpass123";

            System.out.println("Инициализация DatabaseManager...");
            DatabaseManager dbManager = new DatabaseManager(dbHost, dbName, dbUser, dbPass, repoConfig);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Завершение работы сервера. Закрытие пула соединений HikariCP...");
                dbManager.close();
            }));

            System.out.println("Инициализация базы данных...");
            dbManager.init();

            CollectionManager collectionManager = new CollectionManager();

            System.out.println("Загрузка данных из БД в память...");
            List<common.model.HumanBeing> loaded = dbManager.loadAll();
            collectionManager.loadFromDatabase(loaded);
            System.out.println("Загружено элементов: " + loaded.size());

            CommandHandler commandHandler = new CommandHandler(collectionManager, dbManager);

            Thread consoleThread = new Thread(new ServerConsole(commandHandler));
            consoleThread.setDaemon(true);
            consoleThread.start();

            System.out.println("\nСервер готов к работе. Ожидание подключений на порту " + serverConfig.getPort() + "...");
            ConnectionAcceptor acceptor = new ConnectionAcceptor(serverConfig.getPort(), commandHandler);
            acceptor.run();

        } catch (SQLException e) {
            System.err.println("Критическая ошибка работы с базой данных: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}