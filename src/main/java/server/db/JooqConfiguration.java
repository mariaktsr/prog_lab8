package server.db;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

//Конфигурация jOOQ DSLContext.
//DSLContext — единая точка входа для типобезопасных SQL-запросов jOOQ.
//Является потокобезопасным (thread-safe) и предназначен для создания
//одного экземпляра на всё время работы приложения (singleton).
public class JooqConfiguration {

    private final DSLContext dslContext;

    //Создаёт и конфигурирует экземпляр DSLContext
    public JooqConfiguration(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource не может быть null");
        }
        //SQLDialect.POSTGRES включает специфичные функции PostgreSQL
        //(например, RETURNING, JSONB, TIMESTAMPTZ маппинг)
        this.dslContext = DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    //Возвращает настроенный DSLContext для выполнения запросов.
    public DSLContext getDslContext() {
        return dslContext;
    }
}