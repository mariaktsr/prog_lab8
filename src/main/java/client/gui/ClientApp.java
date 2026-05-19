package client.gui;

import client.gui.util.I18N;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Locale;

/**
 * Точка входа для JavaFX-клиента.
 * Загружает окно авторизации и инициализирует локализацию.
 */
public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Инициализация локализации (по умолчанию — русский)
        I18N.setLocale(Locale.ENGLISH);

        // 2. Загрузка FXML окна авторизации
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/gui/view/auth-view.fxml"),
                I18N.getResourceBundle()
        );

        Parent root = loader.load();

        // 3. Настройка сцены и окна
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle(I18N.get("window.auth.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Очистка при закрытии приложения
        System.out.println("GUI-клиент завершён");
    }

    public static void main(String[] args) {
        launch(args);
    }
}