package server.network;

import common.request.Request;
import common.response.Response;
import server.handler.CommandHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ForkJoinPool;

//Модуль приёма подключений и маршрутизации запросов
//Реализует многопоточную архитектуру:
//1. Чтение запроса -> новый Thread
//2. Обработка запроса -> ForkJoinPool
//3. Отправка ответа -> новый Thread
public class ConnectionAcceptor implements Runnable {

    private final int port;
    private final CommandHandler commandHandler;
    private final RequestReader requestReader;
    private final ResponseSender responseSender;

    //Пул для обработки бизнес-логики команд
    private final ForkJoinPool processingPool = new ForkJoinPool();
    private volatile boolean running = true;

    public ConnectionAcceptor(int port, CommandHandler commandHandler) {
        this.port = port;
        this.commandHandler = commandHandler;
        this.requestReader = new RequestReader();
        this.responseSender = new ResponseSender();
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress(port));
            System.out.println("Сервер запущен на порту " + port);

            while (running) {
                SocketChannel clientChannel = serverChannel.accept();
                if (clientChannel == null) continue;

                System.out.println("Подключён клиент: " + clientChannel.getRemoteAddress());

                //многопоточное чтение -> создание нового потока
                new Thread(() -> handleClientLifecycle(clientChannel),
                        "Reader-" + clientChannel.getRemoteAddress()).start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Ошибка серверного сокета: " + e.getMessage());
            }
        }
    }

    //Жизненный цикл клиента: чтение -> постановка в очередь обработки -> ожидание отправки
    private void handleClientLifecycle(SocketChannel clientChannel) {
        try {
            // В потоке чтения можно переключить канал в блокирующий режим для надёжности
            clientChannel.configureBlocking(true);

            while (running && clientChannel.isOpen()) {
                Request request = requestReader.read(clientChannel);
                if (request == null) break; //клиент закрыл соединение

                System.out.println("Получен запрос: " + request.getCommandType() + " от " + clientChannel.getRemoteAddress());

                //обработка запроса через ForkJoinPool
                processingPool.submit(() -> {
                    try {
                        Response response = commandHandler.handle(request, false);

                        //отправка ответа -> создание нового потока
                        new Thread(() -> {
                            try {
                                responseSender.send(clientChannel, response);
                                System.out.println("Ответ успешно отправлен");
                            } catch (IOException e) {
                                System.err.println("Ошибка отправки ответа: " + e.getMessage());
                            }
                        }, "Writer-" + System.currentTimeMillis()).start();

                    } catch (Exception e) {
                        System.err.println("Ошибка обработки команды: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Ошибка соединения с клиентом: " + e.getMessage());
        } finally {
            safeClose(clientChannel);
            System.out.println("Соединение закрыто: " + clientChannel);
        }
    }

    //Метод для безопасного закрытия
    private void safeClose(SocketChannel channel) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
        }
    }

    //Метод остановки сервера
    public void stop() {
        running = false;
    }
}