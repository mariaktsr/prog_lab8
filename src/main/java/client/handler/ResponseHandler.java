package client.handler;

import common.model.User;
import common.response.Response;

import java.util.List;

//Модуль обработки и вывода ответов от сервера

public class ResponseHandler {

    public void handle(Response response) {
        System.out.println("ResponseHandler получил ответ");

        if (response == null) {
            System.err.println("Получен пустой ответ от сервера");
            return;
        }

        if (response.getMessage() != null && !response.getMessage().isEmpty()) {
            if (response.isSuccess()) {
                System.out.println("Успех: " + response.getMessage());
            } else {
                System.err.println("Ошибка: " + response.getMessage());
            }
        }

        Object data = response.getData(Object.class);
        if (data != null) {
            if (data instanceof User) {
                //Игнорируем вывод объекта User, так как авторизация уже отображена в ConsoleReader
                return;
            }

            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.isEmpty()) {
                    System.out.println("Список пуст");
                } else {
                    System.out.println("Элементов: " + list.size());
                    list.forEach(System.out::println);
                }
            } else {
                String dataStr = data.toString();
                if (!dataStr.isEmpty()) {
                    System.out.println(dataStr);
                }
            }
        }
    }
}