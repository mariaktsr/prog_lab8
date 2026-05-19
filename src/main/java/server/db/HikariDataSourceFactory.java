package server.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

//Фабрика для создания оптимизированного пула соединений HikariCP

public class HikariDataSourceFactory {

    //Настройки пула соединений

    //Максимальное количество соединений в пуле
    private static final int DEFAULT_POOL_SIZE = 10;

    //Минимальное количество простаивающих соединений
    private static final int MINIMUM_IDLE = 2;

    //Таймаут ожидания получения соединения из пула (30 секунд)
    private static final long CONNECTION_TIMEOUT_MS = 30_000;

    //Время, после которого простаивающее соединение закрывается (10 минут)
    private static final long IDLE_TIMEOUT_MS = 600_000;

    //Максимальное время жизни соединения в пуле (30 минут).
    private static final long MAX_LIFETIME_MS = 1_800_000;

    //Приватный конструктор предотвращает создание экземпляров
    private HikariDataSourceFactory() {}

    //Создаёт и настраивает DataSource с пулом соединений HikariCP.
    public static DataSource createDataSource(String host, String dbName,
                                              String user, String password) {
        HikariConfig config = new HikariConfig();

        //Базовые настройки подключения
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:5432/%s", host, dbName));
        config.setUsername(user);
        config.setPassword(password);

        //Настройки пула соединений
        config.setMaximumPoolSize(DEFAULT_POOL_SIZE);
        config.setMinimumIdle(MINIMUM_IDLE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        //Оптимизации для PostgreSQL
        //Включение кэширования подготовленных выражений на стороне сервера
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        //Дополнительные опции PostgreSQL для производительности
        config.addDataSourceProperty("stringtype", "unspecified");

        //Название пула для метрик и логирования
        config.setPoolName("HumanBeingAppPool");

        //Проверка соединений при получении из пула
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    //Корректно закрывает пул соединений, освобождая все ресурсы
    public static void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            System.out.println("Закрытие пула соединений: " + hikariDataSource.getPoolName());
            hikariDataSource.close();
        } else if (dataSource != null) {
            throw new IllegalStateException(
                    "DataSource не является экземпляром HikariDataSource. " +
                            "Используйте HikariDataSourceFactory для создания."
            );
        }
    }
}