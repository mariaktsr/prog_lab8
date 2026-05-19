package server.repository.jooq.proxy;

import org.jooq.DSLContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

//для реализации транзакционности через динамический прокси
public class TransactionalInterceptor implements InvocationHandler {

    private final Object target;
    private final DSLContext dslContext;

    public TransactionalInterceptor(Object target, DSLContext dslContext) {
        this.target = target;
        this.dslContext = dslContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Если метод не аннотирован @Transactional, выполняем его напрямую
        if (!method.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        Transactional annotation = method.getAnnotation(Transactional.class);

        try {
            //dslContext.transactionResult() гарантирует атомарность:
            //либо все изменения сохраняются (COMMIT), либо полностью откатываются (ROLLBACK).
            return dslContext.transactionResult(config -> {
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    //Проверяем, нужно ли откатывать транзакцию для этого типа исключения
                    boolean shouldRollback = Arrays.stream(annotation.noRollbackFor())
                            .noneMatch(cls -> cls.isInstance(cause));

                    if (shouldRollback) {
                        //Выбрасываем RuntimeException, чтобы jOOQ автоматически выполнил ROLLBACK
                        throw new RuntimeException("Transactional failure", cause);
                    }
                    //Если исключение в noRollbackFor, jOOQ сделает COMMIT, а мы пробросим ошибку дальше
                    throw new RuntimeException("NoRollbackMarker", cause);
                }
            });
        } catch (RuntimeException e) {
            //Извлекаем оригинальное бизнес-исключение из обёртки
            Throwable cause = e.getCause();

            if (cause != null && "NoRollbackMarker".equals(e.getMessage())) {
                //Транзакция зафиксирована, пробрасываем исключение дальше в CommandHandler
                throw cause;
            }

            //Стандартная обработка после ROLLBACK
            if (cause instanceof RuntimeException re) throw re;
            if (cause instanceof Error err) throw err;
            throw new RuntimeException("Ошибка управления транзакцией", cause);
        }
    }
}