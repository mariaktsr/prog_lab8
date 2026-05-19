package server.network;

import common.request.Request;
import common.util.SerializationHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RequestReader {

    private static final int HEADER_SIZE = 4; //под длину сообщения
    private static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024;

    public Request read(SocketChannel channel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
        if (readFully(channel, header) == -1) {
            return null;
        }
        header.flip();
        int payloadLength = header.getInt();

        if (payloadLength <= 0 || payloadLength > MAX_MESSAGE_SIZE) {
            throw new IOException("Некорректный размер сообщения: " + payloadLength);
        }

        //динамический буфер (выделяем ровно столько, сколько нужно)
        ByteBuffer payload = ByteBuffer.allocate(payloadLength);
        if (readFully(channel, payload) == -1) {
            throw new IOException("Соединение разорвано во время чтения данных");
        }
        payload.flip();

        byte[] data = new byte[payload.remaining()];
        payload.get(data);

        try {
            return SerializationHelper.fromBytes(data, Request.class);
        } catch (ClassNotFoundException e) {
            throw new IOException("Ошибка десериализации Request: " + e.getMessage(), e);
        }
    }

    //гарантированно читает данные, пока буфер не заполнится полностью
    //автоматически собирает TCP-фрагменты (пакеты) в единое сообщение
    private int readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        int totalRead = 0;
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) {
                if (totalRead == 0) {
                    return -1;
                } else {
                    return totalRead;
                }
            }
            totalRead += read;
        }
        return totalRead;
    }
}