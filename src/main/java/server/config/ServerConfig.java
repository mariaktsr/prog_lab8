package server.config;

//Конфигурация сервера

public class ServerConfig {

    private static final String ENV_PORT = "SERVER_PORT";
    private static final int DEFAULT_PORT = 12345;

    private final int port;

    public ServerConfig() {
        // Чтение порта
        int tempPort = DEFAULT_PORT;
        String portStr = System.getenv(ENV_PORT);

        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                tempPort = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                System.err.println("Неверный порт: " + portStr +
                        ", используется порт по умолчанию: " + DEFAULT_PORT);
            }
        }

        this.port = tempPort;
    }

    public int getPort() {
        return port;
    }
}