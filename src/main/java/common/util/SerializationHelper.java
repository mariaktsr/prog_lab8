package common.util;

import java.io.*;

//Утилитный класс для сериализации/десериализации объектов

public class SerializationHelper {

    private SerializationHelper() {
    }

    //Сериализует объект в массив байтов
    public static byte[] toBytes(Serializable obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        }
    }

    //Десериализует объект из массива байтов
    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] data, Class<T> type)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            if (type.isInstance(obj)) {
                return (T) obj;
            }
            throw new ClassCastException("Неверный тип объекта: " + obj.getClass());
        }
    }
}