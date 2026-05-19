package server.repository.jooq.proxy;

import org.jooq.DSLContext;

import java.lang.reflect.Proxy;

//Фабрика для создания динамических прокси-объектов репозиториев

public class RepositoryProxyFactory {

    private RepositoryProxyFactory() {}

    //Создаёт прокси-объект для указанного репозитория, автоматически
    //применяя аспект транзакционности ко всем методам, помеченным @Transactional.
    @SuppressWarnings("unchecked")
    public static <T> T createTransactionalProxy(T target, DSLContext dslContext) {
        if (target == null) {
            throw new IllegalArgumentException("Целевой объект репозитория не может быть null");
        }
        if (dslContext == null) {
            throw new IllegalArgumentException("DSLContext не может быть null");
        }

        //Создаём обработчик, который будет перехватывать вызовы методов
        TransactionalInterceptor interceptor = new TransactionalInterceptor(target, dslContext);

        //Генерируем прокси-объект на лету через JDK Dynamic Proxy
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                interceptor
        );
    }
}