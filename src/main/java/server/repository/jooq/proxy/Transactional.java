package server.repository.jooq.proxy;

import java.lang.annotation.*;

//Пользовательская аннотация для декларативного управления транзакциями
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

    //Массив классов исключений, при возникновении которых транзакция
    //(не должна откатываться, а должна быть зафиксирована)
    Class<? extends Throwable>[] noRollbackFor() default {};
}