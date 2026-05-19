package server.db;

import common.model.HumanBeing;
import server.config.RepositoryConfig;
import server.repository.HumanBeingRepository;
import server.repository.UserRepository;
import server.repository.jooq.JooqHumanBeingRepository;
import server.repository.jooq.JooqUserRepository;
import server.repository.jooq.proxy.RepositoryProxyFactory;
import server.repository.legacy.LegacyHumanBeingRepository;
import server.repository.legacy.LegacyUserRepository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

    private final HumanBeingRepository humanBeingRepo;
    private final UserRepository userRepo;
    private final DataSource dataSource;

    //Инициализирует менеджер БД, создаёт пул соединений и выбирает реализацию репозиториев.
    public DatabaseManager(String host, String dbName, String user, String password,
                           RepositoryConfig repoConfig) throws SQLException {
        this.dataSource = HikariDataSourceFactory.createDataSource(host, dbName, user, password);

        if (repoConfig.isJooq()) {
            //Конфигурация jOOQ
            JooqConfiguration jooqConfig = new JooqConfiguration(dataSource);

            //Создаём "чистые" jOOQ-репозитории
            JooqHumanBeingRepository jooqHumanRepo = new JooqHumanBeingRepository(jooqConfig.getDslContext());
            JooqUserRepository jooqUserRepo = new JooqUserRepository(jooqConfig.getDslContext());

            //Оборачиваем их в динамический прокси для автоматических транзакций (@Transactional)
            this.humanBeingRepo = RepositoryProxyFactory.createTransactionalProxy(jooqHumanRepo, jooqConfig.getDslContext());
            this.userRepo = RepositoryProxyFactory.createTransactionalProxy(jooqUserRepo, jooqConfig.getDslContext());

            System.out.println("[DB] Используется jOOQ-реализация с динамическим проксированием транзакций.");
        } else {
            //Legacy-реализация (PreparedStatement)
            this.humanBeingRepo = new LegacyHumanBeingRepository(dataSource);
            this.userRepo = new LegacyUserRepository(dataSource);

            System.out.println("[DB] Используется Legacy-реализация (JDBC).");
        }
    }

    public void init() throws SQLException {
        userRepo.initSchema();
        humanBeingRepo.initSchema();
        System.out.println("[DB] Схема базы данных успешно инициализирована.");
    }

    //операции с пользователями (делегирование в UserRepository)
    public boolean register(String login, String passwordHash) throws SQLException {
        return userRepo.register(login, passwordHash);
    }

    public String authenticate(String login, String passwordHash) throws SQLException {
        return userRepo.findPasswordHashByLogin(login)
                .filter(storedHash -> storedHash.equals(passwordHash))
                .map(h -> login) //если хэши совпали, возвращаем логин
                .orElse(null);   //иначе null
    }

    //Операции с коллекцией (делегирование в HumanBeingRepository)
    public List<HumanBeing> loadAll() throws SQLException {
        return humanBeingRepo.findAll();
    }

    public boolean save(HumanBeing human) throws SQLException {
        return humanBeingRepo.save(human);
    }

    public boolean update(HumanBeing human) throws SQLException {
        return humanBeingRepo.update(human);
    }

    public boolean deleteById(Long id) throws SQLException {
        return humanBeingRepo.deleteById(id);
    }

    public void saveAllToDatabase(List<HumanBeing> collection) throws SQLException {
        humanBeingRepo.saveAll(collection);
    }

    public long getNextIdFromSequence() throws SQLException {
        return humanBeingRepo.getNextId();
    }

    //Корректно закрывает пул соединений при завершении работы сервера.
    public void close() {
        HikariDataSourceFactory.closeDataSource(dataSource);
    }

    // Геттеры доступны для прямой передачи репозиториев в будущие сервисы
    public HumanBeingRepository getHumanBeingRepo() { return humanBeingRepo; }
    public UserRepository getUserRepo() { return userRepo; }
}