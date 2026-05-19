package server.network;

import common.response.Response;
import common.util.SerializationHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ResponseSender {

    private static final int HEADER_SIZE = 4;

    public void send(SocketChannel channel, Response response) throws IOException {

        byte[] data = SerializationHelper.toBytes(response);

        //динамический буфер: размер = заголовок + данные
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + data.length);
        buffer.putInt(data.length); //записываем длину
        buffer.put(data);           //записываем данные
        buffer.flip();

        //отправляем полностью
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }
}