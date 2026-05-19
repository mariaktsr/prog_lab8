package common.response;

import java.io.Serializable;

//Ответ сервера клиенту

public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Serializable data;


    public Response(boolean success, String message, Serializable data) {
        this.success = success;
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }
        this.data = data;
    }

    public static Response success(String message) {
        return new Response(true, message, null);
    }
    public static Response success(String message, Serializable data) {
        return new Response(true, message, data);
    }
    public static Response error(String message) {
        return new Response(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> type) {
        if (data != null && type.isInstance(data)) {
            return (T) data;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Response{success=" + success + ", message='" + message + '\'' + ", hasData=" + (data != null) + '}';
    }
}