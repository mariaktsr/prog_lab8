package client.gui.controller;

import client.gui.canvas.CanvasRenderer;
import client.gui.dialog.HumanBeingDialog;
import client.gui.service.CollectionSyncService;
import client.gui.util.I18N;
import client.network.NetworkClient;
import common.commands.CommandType;
import common.model.HumanBeing;
import common.model.User;
import common.request.Request;
import common.response.Response;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TableView<HumanBeing> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<Locale> languageSelector;
    @FXML private Label userLabel;
    @FXML private Canvas canvas;
    @FXML private AnchorPane canvasContainer;

    @FXML private TableColumn<HumanBeing, Long> colId;
    @FXML private TableColumn<HumanBeing, String> colName;
    @FXML private TableColumn<HumanBeing, String> colCoordinates;
    @FXML private TableColumn<HumanBeing, String> colCreationDate;
    @FXML private TableColumn<HumanBeing, String> colRealHero;
    @FXML private TableColumn<HumanBeing, String> colWeapon;
    @FXML private TableColumn<HumanBeing, String> colMood;
    @FXML private TableColumn<HumanBeing, String> colOwner;

    private User currentUser;
    private NetworkClient networkClient;
    private ObservableList<HumanBeing> tableData = FXCollections.observableArrayList();
    private CanvasRenderer canvasRenderer;
    private CollectionSyncService syncService;

    // Поля для сортировки через Streams API
    private String sortField = "id";
    private boolean sortAscending = true;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableLogic();
        setupLocalization();
    }

    public void initData(User user, NetworkClient client) {
        this.currentUser = user;
        this.networkClient = client;
        userLabel.setText("User: " + user.getLogin());

        canvasRenderer = new CanvasRenderer(canvas);
        canvasRenderer.setClickHandler(this::handleCanvasClick);
        canvasRenderer.startRenderingLoop();

        syncService = new CollectionSyncService(networkClient, currentUser, tableData, canvasRenderer);
        syncService.start(1, 3);

        loadCollection();
    }

    private void loadCollection() {
        if (currentUser == null || networkClient == null) return;

        new Thread(() -> {
            try {
                Request req = new Request(CommandType.SHOW, new String[0], null, currentUser);
                Response resp = networkClient.sendRequest(req);

                if (resp.isSuccess() && resp.getData(List.class) != null) {
                    List<?> rawList = resp.getData(List.class);
                    List<HumanBeing> humans = rawList.stream()
                            .filter(o -> o instanceof HumanBeing)
                            .map(o -> (HumanBeing) o)
                            .collect(Collectors.toList());

                    Platform.runLater(() -> {
                        tableData.setAll(humans);
                        table.setItems(tableData);

                        if (canvasRenderer != null) {
                            canvasRenderer.syncData(humans);
                        }

                        // Применяем текущий поиск если есть
                        String searchText = searchField.getText();
                        if (searchText != null && !searchText.isEmpty()) {
                            filterTableWithStreams(searchText);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getName()));
        colCoordinates.setCellValueFactory(c -> {
            if (c.getValue().getCoordinates() == null) return new ReadOnlyStringWrapper("");
            return new ReadOnlyStringWrapper("(" + c.getValue().getCoordinates().getX() + ", " +
                    c.getValue().getCoordinates().getY() + ")");
        });
        colCreationDate.setCellValueFactory(c -> {
            if (c.getValue().getCreationDate() == null) return new ReadOnlyStringWrapper("");
            ZonedDateTime date = c.getValue().getCreationDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", I18N.getLocale())
                    .withZone(ZoneId.systemDefault());
            return new ReadOnlyStringWrapper(date.format(formatter));
        });
        colRealHero.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getRealHero().toString()));
        colWeapon.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getWeaponType().toString()));
        colMood.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getMood().toString()));
        colOwner.setCellValueFactory(c -> {
            if (c.getValue().getOwner() == null) return new ReadOnlyStringWrapper("");
            return new ReadOnlyStringWrapper(c.getValue().getOwner().getLogin());
        });
    }

    // === 🔍 ФИЛЬТРАЦИЯ ЧЕРЕЗ STREAMS API ===
    private void filterTableWithStreams(String searchText) {
        List<HumanBeing> result;

        if (searchText == null || searchText.trim().isEmpty()) {
            result = new ArrayList<>(tableData);
        } else {
            String lower = searchText.toLowerCase().trim();

            result = tableData.stream()
                    .filter(h -> {
                        boolean nameMatch = h.getName() != null && h.getName().toLowerCase().contains(lower);
                        boolean ownerMatch = h.getOwner() != null && h.getOwner().getLogin() != null &&
                                h.getOwner().getLogin().toLowerCase().contains(lower);
                        boolean weaponMatch = h.getWeaponType() != null &&
                                h.getWeaponType().toString().toLowerCase().contains(lower);
                        boolean moodMatch = h.getMood() != null &&
                                h.getMood().toString().toLowerCase().contains(lower);
                        boolean coordsMatch = h.getCoordinates() != null &&
                                (h.getCoordinates().getX() + "").contains(lower);
                        boolean realHeroMatch = (h.getRealHero() + "").toLowerCase().contains(lower);
                        boolean dateMatch = h.getCreationDate() != null &&
                                h.getCreationDate().toString().toLowerCase().contains(lower);
                        boolean idMatch = (h.getId() + "").contains(lower);

                        return nameMatch || ownerMatch || weaponMatch || moodMatch ||
                                coordsMatch || realHeroMatch || dateMatch || idMatch;
                    })
                    .collect(Collectors.toList());
        }

        table.setItems(FXCollections.observableArrayList(result));
    }

    // === 📊 СОРТИРОВКА ЧЕРЕЗ STREAMS API ===
    private void setupTableLogic() {
        // 1. Фильтрация при вводе в поиск
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterTableWithStreams(newVal);
        });

        // 2. Настраиваем клики по заголовкам (через Label, т.к. TableColumn не имеет setOnMouseClicked)
        setupSortableColumn(colId, "id");
        setupSortableColumn(colName, "name");
        setupSortableColumn(colCoordinates, "coordinates");
        setupSortableColumn(colCreationDate, "creationDate");
        setupSortableColumn(colRealHero, "realHero");
        setupSortableColumn(colWeapon, "weaponType");
        setupSortableColumn(colMood, "mood");
        setupSortableColumn(colOwner, "owner");

        // 3. Двойной клик по строке для редактирования
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                HumanBeing selected = table.getSelectionModel().getSelectedItem();
                openEditDialog(selected);
            }
        });

        // Инициализация
        table.setItems(tableData);
    }

    // Вспомогательный метод для создания кликабельного заголовка
    private void setupSortableColumn(TableColumn<HumanBeing, ?> col, String fieldName) {
        Label label = new Label(col.getText());
        label.setStyle("-fx-cursor: hand; -fx-padding: 2px 0 2px 5px; -fx-alignment: CENTER_LEFT;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);

        label.setOnMouseClicked(e -> {
            sortField = fieldName;
            sortAscending = !sortAscending;
            applySortWithStreams();
        });

        col.setGraphic(label);
        col.setSortable(false); // Отключаем стандартную сортировку JavaFX
        col.setPrefWidth(col.getPrefWidth()); // Фиксируем ширину
    }

    private void applySortWithStreams() {
        List<HumanBeing> currentList = table.getItems();

        Comparator<HumanBeing> comparator = switch (sortField) {
            case "id" -> Comparator.comparing(HumanBeing::getId);
            case "name" -> Comparator.comparing(h -> h.getName() != null ? h.getName() : "", String.CASE_INSENSITIVE_ORDER);
            case "coordinates" -> Comparator.comparing(h -> h.getCoordinates() != null ? h.getCoordinates().getX() : 0.0);
            case "creationDate" -> Comparator.comparing(HumanBeing::getCreationDate);
            case "realHero" -> Comparator.comparing(HumanBeing::getRealHero);
            case "weaponType" -> Comparator.comparing(h -> h.getWeaponType() != null ? h.getWeaponType().toString() : "");
            case "mood" -> Comparator.comparing(h -> h.getMood() != null ? h.getMood().toString() : "");
            case "owner" -> Comparator.comparing(h -> h.getOwner() != null && h.getOwner().getLogin() != null ? h.getOwner().getLogin() : "");
            default -> Comparator.comparing(HumanBeing::getId);
        };

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        List<HumanBeing> sorted = currentList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(sorted));
    }

    private void setupLocalization() {
        languageSelector.setItems(FXCollections.observableArrayList(I18N.getSupportedLocales()));
        languageSelector.setValue(I18N.getLocale());

        I18N.localeProperty().addListener((obs, o, n) -> languageSelector.setValue(n));
        languageSelector.setOnAction(e -> {
            Locale sel = languageSelector.getValue();
            if (sel != null) I18N.setLocale(sel);
        });
    }

    // === 🎯 КОМАНДЫ ===

    @FXML private void handleShow() { loadCollection(); }

    @FXML private void handleHelp() {
        sendCommandRequest(CommandType.HELP, new String[0], null, "Справка");
    }

    @FXML private void handleInfo() {
        sendCommandRequest(CommandType.INFO, new String[0], null, "Информация о коллекции");
    }

    @FXML private void handleClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Очистить ВСЮ коллекцию?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sendCommandRequest(CommandType.CLEAR, new String[0], null, "Очистка", () -> loadCollection());
        }
    }

    @FXML private void handleSort() {
        sendCommandRequest(CommandType.SORT, new String[0], null, "Сортировка", () -> loadCollection());
    }

    @FXML private void handleSumOfImpactSpeed() {
        sendCommandRequest(CommandType.SUM_OF_IMPACT_SPEED, new String[0], null, "Сумма скорости удара");
    }

    @FXML private void handlePrintFieldDescendingMood() {
        sendCommandRequest(CommandType.PRINT_FIELD_DESCENDING_MOOD, new String[0], null, "Mood (по убыванию)");
    }

    @FXML private void handleFilterContainsName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Фильтр по имени");
        dialog.setHeaderText("Введите часть имени:");
        dialog.setContentText("Имя:");

        dialog.showAndWait().ifPresent(name ->
                sendCommandRequest(CommandType.FILTER_CONTAINS_NAME, new String[]{name}, null, "Фильтр")
        );
    }

    @FXML private void handleRemoveAt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удалить по индексу");
        dialog.setHeaderText("Введите индекс (0, 1, 2...):");
        dialog.setContentText("Индекс:");

        dialog.showAndWait().ifPresent(index ->
                sendCommandRequest(CommandType.REMOVE_AT, new String[]{index}, null, "Удаление", () -> loadCollection())
        );
    }

    @FXML private void handleRemoveById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Удалить по ID");
        dialog.setHeaderText("Введите ID:");
        dialog.setContentText("ID:");

        dialog.showAndWait().ifPresent(id ->
                sendCommandRequest(CommandType.REMOVE_BY_ID, new String[]{id}, null, "Удаление", () -> loadCollection())
        );
    }

    @FXML private void handleExecuteScript() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите скрипт");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Script Files", "*.txt"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            sendCommandRequest(CommandType.EXECUTE_SCRIPT, new String[]{file.getAbsolutePath()}, null, "Скрипт");
        }
    }

    @FXML private void handleInsertAt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Вставить по индексу");
        dialog.setHeaderText("Введите индекс:");
        dialog.setContentText("Индекс:");

        dialog.showAndWait().ifPresent(index -> {
            HumanBeingDialog hbDialog = new HumanBeingDialog(null, currentUser);
            hbDialog.showAndWait().ifPresent(human ->
                    sendCommandRequest(CommandType.INSERT_AT, new String[]{index}, human, "Вставка", () -> loadCollection())
            );
        });
    }

    @FXML private void handleAdd() {
        HumanBeingDialog dialog = new HumanBeingDialog(null, currentUser);
        dialog.showAndWait().ifPresent(newHuman ->
                sendCommandRequest(CommandType.ADD, new String[0], newHuman, "Добавление", () -> loadCollection())
        );
    }

    @FXML private void handleUpdate() {
        HumanBeing selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите объект для обновления");
            return;
        }
        openEditDialog(selected);
    }

    @FXML private void handleRemove() {
        HumanBeing selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите объект для удаления");
            return;
        }
        if (!selected.getOwner().getLogin().equals(currentUser.getLogin())) {
            showError("Только владелец может удалить объект!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Удалить \"" + selected.getName() + "\"?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sendCommandRequest(CommandType.REMOVE_BY_ID, new String[]{selected.getId().toString()}, null, "Удаление", () -> loadCollection());
        }
    }

    @FXML private void handleLogout() {
        if (syncService != null) syncService.stop();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/gui/view/auth-view.fxml"), I18N.getResourceBundle());
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === 📡 Вспомогательный метод для отправки команд ===
    private void sendCommandRequest(CommandType cmd, String[] args, Object data, String title) {
        sendCommandRequest(cmd, args, data, title, null);
    }

    private void sendCommandRequest(CommandType cmd, String[] args, Object data, String title, Runnable onSuccess) {
        new Thread(() -> {
            try {
                // Проверяем, что data реализует Serializable
                java.io.Serializable serializableData = null;
                if (data instanceof java.io.Serializable) {
                    serializableData = (java.io.Serializable) data;
                }

                Request req = new Request(cmd, args, serializableData, currentUser);
                Response resp = networkClient.sendRequest(req);

                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        if (onSuccess != null) onSuccess.run();
                        else showAlert(title, resp.getMessage());
                    } else {
                        showError(resp.getMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Ошибка сети: " + e.getMessage()));
            }
        }).start();
    }

    // === 🎨 Canvas и диалоги ===
    private void handleCanvasClick(HumanBeing human) {
        if (human == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация об объекте");
        alert.setHeaderText(human.getName() + " (ID: " + human.getId() + ")");
        alert.setContentText("Владелец: " + human.getOwner().getLogin() + "\nКоординаты: " + human.getCoordinates());
        alert.showAndWait();
        if (human.getOwner().getLogin().equals(currentUser.getLogin())) {
            openEditDialog(human);
        }
    }

    private void openEditDialog(HumanBeing human) {
        HumanBeingDialog dialog = new HumanBeingDialog(human, currentUser);
        dialog.showAndWait().ifPresent(updated ->
                sendCommandRequest(CommandType.UPDATE, new String[]{updated.getId().toString()}, updated, "Обновление", () -> loadCollection())
        );
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}