package client.network;

import common.request.Request;
import common.response.Response;
import common.util.SerializationHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

//Клиентский модуль для сетевого взаимодействия с сервером
//(использует блокирующий режим)

public class NetworkClient {

    private SocketChannel channel;
    private final String host;
    private final int port;
    private boolean connected = false;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //Подключается к серверу
    public boolean connect() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true); //Блокирующий режим

            InetSocketAddress address = new InetSocketAddress(host, port);

            System.out.println("Подключение к серверу " + host + ":" + port + "...");

            //Блокирующее подключение
            channel.connect(address);

            connected = true;
            System.out.println("Подключено к серверу");
            return true;

        } catch (IOException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
            System.err.println("Убедитесь, что сервер запущен на порту " + port);
            connected = false;
            return false;
        }
    }

    //Отправляет запрос на сервер и получает ответ
    public Response sendRequest(Request request) throws IOException {
        if (!connected || channel == null || !channel.isConnected()) {
            throw new IOException("Нет подключения к серверу");
        }

        try {
            //сериализация и отправка запроса
            byte[] requestData = SerializationHelper.toBytes(request);
            ByteBuffer requestBuffer = ByteBuffer.allocate(4 + requestData.length);
            requestBuffer.putInt(requestData.length);
            requestBuffer.put(requestData);
            requestBuffer.flip();

            while (requestBuffer.hasRemaining()) {
                channel.write(requestBuffer);
            }
            System.out.println("Запрос отправлен (" + requestData.length + " байт)");

            //чтение заголовка ответа (4 байта)
            ByteBuffer headerBuffer = ByteBuffer.allocate(4);
            int headerBytes = readFully(channel, headerBuffer);
            if (headerBytes == -1) throw new IOException("Сервер закрыл соединение");

            headerBuffer.flip();
            int responseLength = headerBuffer.getInt();

            if (responseLength <= 0 || responseLength > 10_000_000) {
                throw new IOException("Некорректный размер ответа: " + responseLength);
            }

            //динамический буфер под ответ
            ByteBuffer responseBuffer = ByteBuffer.allocate(responseLength);
            int dataBytes = readFully(channel, responseBuffer);
            if (dataBytes == -1 || dataBytes < responseLength) {
                throw new IOException("Неполный ответ от сервера");
            }
            responseBuffer.flip();

            byte[] responseData = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseData);

            //десериализация
            try {
                Response response = SerializationHelper.fromBytes(responseData, Response.class);
                System.out.println("Ответ получен: " + (response.isSuccess() ? "OK" : "ERROR"));
                return response;
            } catch (ClassNotFoundException e) {
                throw new IOException("Ошибка десериализации ответа", e);
            }

        } catch (IOException e) {
            connected = false;
            throw new IOException("Ошибка связи с сервером: " + e.getMessage(), e);
        }
    }

    //гарантированно читает данные из сети, пока буфер не заполнится
    private int readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        int total = 0;
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) {
                if (total == 0) {
                    return -1;
                } else {
                    return total;
                }
            }
            total += read;
        }
        return total;
    }

    //Проверяет, подключён ли клиент
    public boolean isConnected() {
        return connected && channel != null && channel.isConnected();
    }

    //Отключается от сервера
    public void disconnect() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
                System.out.println("Отключено от сервера");
            } catch (IOException e) {
                System.err.println("Ошибка при отключении: " + e.getMessage());
            }
        }
        connected = false;
    }
}