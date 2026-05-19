package server.config;

import java.util.Arrays;

//Конфигурация слоя доступа к данным
//Доступные значения:
//LEGACY — реализация на PreparedStatement (ручное управление)
//JOOQ — реализация на jOOQ DSL (типобезопасные запросы)
public class RepositoryConfig {

    public enum RepositoryType {
        //Legacy-реализация с использованием PreparedStatement
        LEGACY,

        //Реализация на основе jOOQ DSL
        JOOQ
    }

    private static final String ENV_REPO_TYPE = "REPOSITORY_TYPE";
    private static final RepositoryType DEFAULT_TYPE = RepositoryType.LEGACY;

    private final RepositoryType type;

    public RepositoryConfig() {
        RepositoryType resolvedType = DEFAULT_TYPE;
        String typeEnv = System.getenv(ENV_REPO_TYPE);

        if (typeEnv != null && !typeEnv.trim().isEmpty()) {
            try {
                resolvedType = RepositoryType.valueOf(typeEnv.trim().toUpperCase());
                System.out.println("Используется репозиторий: " + resolvedType);
            } catch (IllegalArgumentException e) {
                System.err.println("Неверный тип репозитория: '" + typeEnv +
                        "'. Доступные значения: " +
                        Arrays.toString(RepositoryType.values()));
                System.err.println("Используется значение по умолчанию: " + DEFAULT_TYPE);
            }
        } else {
            System.out.println("REPOSITORY_TYPE не задан, используется по умолчанию: " + DEFAULT_TYPE);
        }

        this.type = resolvedType;
    }

    public RepositoryType getType() {
        return type;
    }

    public boolean isJooq() {
        return type == RepositoryType.JOOQ;
    }

    public boolean isLegacy() {
        return type == RepositoryType.LEGACY;
    }
}