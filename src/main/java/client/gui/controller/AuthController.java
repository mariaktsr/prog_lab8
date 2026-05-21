package client.gui.controller;

import client.gui.ClientApp;
import client.gui.util.I18N;
import client.gui.util.I18NResourceBundle;
import client.network.NetworkClient;
import common.commands.CommandType;
import common.model.User;
import common.request.Request;
import common.response.Response;
import common.util.PasswordHasher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Locale;

public class AuthController {

    // --- FXML элементы ---
    @FXML private VBox rootVBox;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private ComboBox<Locale> languageSelector;

    // --- Логика ---
    private NetworkClient networkClient;
    private User currentUser;

    @FXML
    public void initialize() {
        // 1. Инициализация сетевого клиента
        networkClient = new NetworkClient("localhost", 12345); // Порт по умолчанию

        // 2. Настройка локализации
        setupLocalization();

        // 3. Попытка подключения к серверу при старте
        connectToServer();
    }

    private void setupLocalization() {
        languageSelector.setItems(FXCollections.observableArrayList(I18N.getSupportedLocales()));
        languageSelector.setValue(I18N.getLocale());

        // ✅ Кастомный рендеринг названий
        languageSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18N.getLocaleDisplayName(item));
            }
        });

        languageSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18N.getLocaleDisplayName(item));
            }
        });

        // ✅ Обработчик выбора
        languageSelector.setOnAction(e -> {
            Locale selected = languageSelector.getValue();
            if (selected != null && !selected.equals(I18N.getLocale())) {
                I18N.setLocale(selected);
                // updateUITexts() вызовется автоматически через listener ниже
            }
        });

        // ✅ Глобальный слушатель изменений локали
        I18N.localeProperty().addListener((obs, oldLocale, newLocale) -> {
            Platform.runLater(this::updateUITexts);
            languageSelector.setValue(newLocale);
        });
    }

    private void updateUITexts() {
        // Обновляем все текстовые элементы
        if (loginField != null)
            loginField.setPromptText(I18N.get("auth.username"));
        if (passwordField != null)
            passwordField.setPromptText(I18N.get("auth.password"));
        if (loginButton != null)
            loginButton.setText(I18N.get("auth.login"));
        if (registerButton != null)
            registerButton.setText(I18N.get("auth.register"));

        // Обновляем статус если есть пользователь
        if (statusLabel != null && currentUser != null) {
            statusLabel.setText(I18N.get("auth.welcome", currentUser.getLogin()));
        }

        // Обновляем заголовок окна
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        if (stage != null) {
            stage.setTitle(I18N.get("window.auth.title"));
        }

        System.out.println("[AuthController] UI texts updated for locale: " + I18N.getLocale());
    }

    private void connectToServer() {
        new Thread(() -> {
            boolean connected = networkClient.connect();
            Platform.runLater(() -> {
                if (connected) {
                    // ✅ ИСПОЛЬЗУЕМ I18N ВМЕСТО ХАРДКОДА
                    statusLabel.setText(I18N.get("auth.connected"));
                    statusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    // ✅ ИСПОЛЬЗУЕМ I18N ВМЕСТО ХАРДКОДА
                    statusLabel.setText(I18N.get("auth.not.connected"));
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            });
        }).start();
    }

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText(I18N.get("auth.error.empty"));
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Запускаем в фоновом потоке, чтобы не блокировать UI
        new Thread(() -> {
            String hash = PasswordHasher.hash(password);
            User user = new User(login, hash);
            Request request = new Request(CommandType.LOGIN, new String[0], null, user);

            try {
                Response response = networkClient.sendRequest(request);
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        currentUser = response.getData(User.class);
                        if (currentUser != null) {
                            statusLabel.setText(I18N.get("auth.welcome", currentUser.getLogin()));
                            statusLabel.setStyle("-fx-text-fill: green;");
                            // Переход в главное меню (заглушка)
                            openMainView();
                        }
                    } else {
                        statusLabel.setText(I18N.get("auth.error.failed", response.getMessage()));
                        statusLabel.setStyle("-fx-text-fill: red;");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка сети: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText(I18N.get("auth.error.empty"));
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        new Thread(() -> {
            String hash = PasswordHasher.hash(password);
            User user = new User(login, hash);
            Request request = new Request(CommandType.REGISTER, new String[0], null, user);

            try {
                Response response = networkClient.sendRequest(request);
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        statusLabel.setText("Регистрация успешна! Теперь войдите.");
                        statusLabel.setStyle("-fx-text-fill: blue;");
                        // Автоматический вход после регистрации (опционально)
                        currentUser = user;
                        openMainView();
                    } else {
                        statusLabel.setText(I18N.get("auth.error.failed", response.getMessage()));
                        statusLabel.setStyle("-fx-text-fill: red;");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка сети: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        }).start();
    }

    // Переход к главному экрану
    private void openMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/gui/view/main-view.fxml"),
                    new I18NResourceBundle()
            );
            Parent mainRoot = loader.load();

            // --- ДОБАВИТЬ ЭТИ СТРОКИ ---
            // 1. Получаем экземпляр контроллера
            MainController controller = loader.getController();
            // 2. Передаем данные (пользователя и сетевой клиент)
            controller.initData(currentUser, networkClient);
            // --------------------------

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(mainRoot, 1000, 700));
            stage.setTitle("HumanBeing GUI");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Конвертер для отображения локали в ComboBox
    private static class LocaleStringConverter extends StringConverter<Locale> {
        @Override
        public String toString(Locale object) {
            if (object == null) return "";
            return object.getDisplayLanguage(object);
        }
        @Override
        public Locale fromString(String string) {
            return null; // Не используется
        }
    }
}