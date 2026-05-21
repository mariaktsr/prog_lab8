package client.gui;

import client.gui.util.I18N;
import client.gui.util.I18NResourceBundle;
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
        // 1. Инициализация локализации
        I18N.setLocale(I18N.RU); // или любой другой по умолчанию

        // 2. Загрузка FXML с адаптером ResourceBundle
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/client/gui/view/auth-view.fxml"),
                new I18NResourceBundle() // ✅ Теперь FXML видит ресурсы!
        );

        Parent root = loader.load();

        // 3. Настройка сцены
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle(I18N.get("window.auth.title"));
        primaryStage.setScene(scene);
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