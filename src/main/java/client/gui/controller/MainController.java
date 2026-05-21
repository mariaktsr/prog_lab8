// src/main/java/client/gui/controller/MainController.java
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Основной контроллер главного окна приложения.
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Отображение таблицы объектов с фильтрацией/сортировкой через Streams API</li>
 *   <li>Интеграцию с Canvas для визуализации объектов</li>
 *   <li>Обработку команд пользователя и сетевых запросов</li>
 *   <li>Динамическую локализацию интерфейса (4 языка)</li>
 *   <li>CRUD-операции с объектами HumanBeing</li>
 * </ul>
 */
public class MainController {

    // ========================================================================
    // 🔹 FXML-инъекции: элементы интерфейса
    // ========================================================================

    @FXML private TableView<HumanBeing> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<Locale> languageSelector;
    @FXML private Label userLabel;
    @FXML private Canvas canvas;
    @FXML private AnchorPane canvasContainer;

    // Колонки таблицы
    @FXML private TableColumn<HumanBeing, Long> colId;
    @FXML private TableColumn<HumanBeing, String> colName;
    @FXML private TableColumn<HumanBeing, String> colCoordinates;
    @FXML private TableColumn<HumanBeing, String> colCreationDate;
    @FXML private TableColumn<HumanBeing, String> colRealHero;
    @FXML private TableColumn<HumanBeing, String> colWeapon;
    @FXML private TableColumn<HumanBeing, String> colMood;
    @FXML private TableColumn<HumanBeing, String> colOwner;

    // Кнопки тулбара (если определены в FXML)
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;
    @FXML private Button btnLogout;

    // ========================================================================
    // 🔹 Поля логики
    // ========================================================================

    private User currentUser;
    private NetworkClient networkClient;
    private ObservableList<HumanBeing> tableData = FXCollections.observableArrayList();
    private CanvasRenderer canvasRenderer;
    private CollectionSyncService syncService;

    // Параметры сортировки для Streams API
    private String sortField = "id";
    private boolean sortAscending = true;

    // Форматтеры для отображения данных
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault());

    // ========================================================================
    // 🔹 Инициализация
    // ========================================================================

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableLogic();
        setupLocalization();
    }

    /**
     * Инициализация контроллера данными от авторизации.
     * @param user авторизованный пользователь
     * @param client сетевой клиент для отправки запросов
     */
    public void initData(User user, NetworkClient client) {
        this.currentUser = user;
        this.networkClient = client;

        // Обновляем отображение пользователя
        updateUserLabel();

        // Инициализация Canvas
        initCanvas();

        // Запуск сервиса синхронизации коллекции
        initSyncService();

        // Загрузка коллекции с сервера
        loadCollection();
    }

    private void updateUserLabel() {
        if (currentUser != null && userLabel != null) {
            userLabel.setText(I18N.get("main.user.label") + " " + currentUser.getLogin());
        }
    }

    private void initCanvas() {
        if (canvas != null) {
            canvasRenderer = new CanvasRenderer(canvas);
            canvasRenderer.setClickHandler(this::handleCanvasClick);
            canvasRenderer.startRenderingLoop();
        }
    }

    private void initSyncService() {
        if (networkClient != null && currentUser != null && tableData != null && canvasRenderer != null) {
            syncService = new CollectionSyncService(networkClient, currentUser, tableData, canvasRenderer);
            syncService.start(1, 3); // задержка 1с, период 3с
        }
    }

    // ========================================================================
    // 🔹 Локализация
    // ========================================================================

    private void setupLocalization() {
        languageSelector.setItems(FXCollections.observableArrayList(I18N.getSupportedLocales()));
        languageSelector.setValue(I18N.getLocale());

        // Cell factory для отображения названий
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

        languageSelector.setOnAction(e -> {
            Locale sel = languageSelector.getValue();
            if (sel != null && !sel.equals(I18N.getLocale())) {
                I18N.setLocale(sel);
            }
        });

        // ✅ Глобальный слушатель
        I18N.localeProperty().addListener((obs, oldL, newL) -> {
            Platform.runLater(this::updateAllTexts);
            languageSelector.setValue(newL);
        });
    }

    private void setupLocaleSelector() {
        if (languageSelector == null) return;

        // Заполнение списка доступных локалей
        languageSelector.setItems(FXCollections.observableArrayList(I18N.getSupportedLocales()));
        languageSelector.setValue(I18N.getLocale());

        // ✅ Кастомный рендеринг названий локалей в выпадающем списке
        languageSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18N.getLocaleDisplayName(item));
            }
        });

        // ✅ Кастомный рендеринг выбранной локали в кнопке ComboBox
        languageSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Locale item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : I18N.getLocaleDisplayName(item));
            }
        });

        // Обработчик выбора локали пользователем
        languageSelector.setOnAction(e -> {
            Locale selected = languageSelector.getValue();
            if (selected != null && !selected.equals(I18N.getLocale())) {
                I18N.setLocale(selected);
                // updateAllTexts() вызовется автоматически через localeProperty listener
            }
        });
    }

    /**
     * Обновляет все текстовые элементы интерфейса при смене локали.
     * Вызывается автоматически при изменении I18N.localeProperty.
     */
    private void updateAllTexts() {
        // Заголовок окна
        Stage stage = (Stage) table.getScene().getWindow();
        if (stage != null) {
            stage.setTitle(I18N.get("window.main.title"));
        }

        // Пользователь
        if (currentUser != null && userLabel != null) {
            userLabel.setText(I18N.get("main.user.label") + " " + currentUser.getLogin());
        }

        // Заголовки колонок
        if (colId != null) colId.setText(I18N.get("table.id"));
        if (colName != null) colName.setText(I18N.get("table.name"));
        if (colCoordinates != null) colCoordinates.setText(I18N.get("table.coordinates"));
        if (colCreationDate != null) colCreationDate.setText(I18N.get("table.creationDate"));
        if (colRealHero != null) colRealHero.setText(I18N.get("table.realHero"));
        if (colWeapon != null) colWeapon.setText(I18N.get("table.weaponType"));
        if (colMood != null) colMood.setText(I18N.get("table.mood"));
        if (colOwner != null) colOwner.setText(I18N.get("table.owner"));

        // Поиск
        if (searchField != null) {
            searchField.setPromptText(I18N.get("main.search.placeholder"));
        }

        // Кнопки тулбара
        if (btnRefresh != null) btnRefresh.setText(I18N.get("main.toolbar.refresh"));
        if (btnAdd != null) btnAdd.setText(I18N.get("main.toolbar.add"));
        if (btnDelete != null) btnDelete.setText(I18N.get("main.toolbar.delete"));
        if (btnLogout != null) btnLogout.setText(I18N.get("main.toolbar.logout"));

        // ✅ Перерисовываем таблицу для применения нового форматирования
        table.refresh();

        System.out.println("[MainController] UI texts updated for locale: " + I18N.getLocale());
    }

    private void updateTableHeaders() {
        if (colId != null) colId.setText(I18N.get("table.id"));
        if (colName != null) colName.setText(I18N.get("table.name"));
        if (colCoordinates != null) colCoordinates.setText(I18N.get("table.coordinates"));
        if (colCreationDate != null) colCreationDate.setText(I18N.get("table.creationDate"));
        if (colRealHero != null) colRealHero.setText(I18N.get("table.realHero"));
        if (colWeapon != null) colWeapon.setText(I18N.get("table.weaponType"));
        if (colMood != null) colMood.setText(I18N.get("table.mood"));
        if (colOwner != null) colOwner.setText(I18N.get("table.owner"));
    }

    private void updateToolbarButtons() {
        if (btnAdd != null) btnAdd.setText(I18N.get("main.toolbar.add"));
        if (btnDelete != null) btnDelete.setText(I18N.get("main.toolbar.delete"));
        if (btnRefresh != null) btnRefresh.setText(I18N.get("main.toolbar.refresh"));
        if (btnLogout != null) btnLogout.setText(I18N.get("main.toolbar.logout"));
    }

    // ========================================================================
    // 🔹 Настройка таблицы
    // ========================================================================

    private void setupTableColumns() {
        // === ID ===
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colId.setSortable(true);

        // === Name ===
        colName.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getName() != null ? c.getValue().getName() : "")
        );

        // === Coordinates (с форматированием чисел по локали) ===
        colCoordinates.setCellValueFactory(c -> {
            var coords = c.getValue().getCoordinates();
            if (coords == null) return new ReadOnlyStringWrapper("");
            String x = I18N.formatNumber(coords.getX());
            String y = I18N.formatNumber(coords.getY());
            return new ReadOnlyStringWrapper("(" + x + "; " + y + ")");
        });

        // === Creation Date (с форматированием по локали) ===
        colCreationDate.setCellValueFactory(c -> {
            var date = c.getValue().getCreationDate();
            if (date == null) return new ReadOnlyStringWrapper("");
            String formatted = I18N.formatDate(date);
            return new ReadOnlyStringWrapper(formatted);
        });

        // === Real Hero ===
        colRealHero.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getRealHero() != null
                        ? (c.getValue().getRealHero() ? "✓" : "✗")
                        : "")
        );

        // === Weapon Type ===
        colWeapon.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getWeaponType() != null
                        ? c.getValue().getWeaponType().toString()
                        : "")
        );

        // === Mood ===
        colMood.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getMood() != null
                        ? c.getValue().getMood().toString()
                        : "")
        );

        // === Owner ===
        colOwner.setCellValueFactory(c -> {
            var owner = c.getValue().getOwner();
            return new ReadOnlyStringWrapper(owner != null && owner.getLogin() != null
                    ? owner.getLogin()
                    : "");
        });

        // Настройка ширины колонок
        colId.setPrefWidth(60);
        colName.setPrefWidth(150);
        colCoordinates.setPrefWidth(120);
        colCreationDate.setPrefWidth(160);
        colRealHero.setPrefWidth(80);
        colWeapon.setPrefWidth(100);
        colMood.setPrefWidth(100);
        colOwner.setPrefWidth(120);
    }

    private void setupTableLogic() {
        // === 1. Фильтрация через Streams API при вводе в поиск ===
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filterTableWithStreams(newVal)
        );

        // === 2. Сортировка по клику на заголовок колонки (Streams API) ===
        setupSortableColumn(colId, "id");
        setupSortableColumn(colName, "name");
        setupSortableColumn(colCoordinates, "coordinates");
        setupSortableColumn(colCreationDate, "creationDate");
        setupSortableColumn(colRealHero, "realHero");
        setupSortableColumn(colWeapon, "weaponType");
        setupSortableColumn(colMood, "mood");
        setupSortableColumn(colOwner, "owner");

        // === 3. Двойной клик по строке → редактирование ===
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                HumanBeing selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openEditDialog(selected);
                }
            }
        });

        // === 4. Контекстное меню для быстрого доступа к действиям ===
        setupContextMenu();

        // Инициализация данных таблицы
        table.setItems(tableData);
    }

    /**
     * Настраивает колонку таблицы для сортировки по клику на заголовок.
     * Использует Label вместо текста для обработки кликов.
     */
    private void setupSortableColumn(TableColumn<HumanBeing, ?> col, String fieldName) {
        Label label = new Label(col.getText());
        label.setStyle("-fx-cursor: hand; -fx-padding: 2px 0 2px 5px;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);

        label.setOnMouseClicked(e -> {
            // Переключаем направление сортировки при повторном клике на ту же колонку
            if (fieldName.equals(sortField)) {
                sortAscending = !sortAscending;
            } else {
                sortField = fieldName;
                sortAscending = true;
            }
            applySortWithStreams();
            updateSortIndicator(col);
        });

        col.setGraphic(label);
        col.setSortable(false); // Отключаем стандартную сортировку JavaFX
    }

    /**
     * Обновляет визуальный индикатор сортировки (▲/▼) в заголовке колонки.
     */
    private void updateSortIndicator(TableColumn<HumanBeing, ?> activeCol) {
        // Сбрасываем все заголовки
        for (TableColumn<HumanBeing, ?> col : table.getColumns()) {
            if (col.getGraphic() instanceof Label label) {
                label.setText(col.getText());
            }
        }
        // Добавляем индикатор к активной колонке
        if (activeCol.getGraphic() instanceof Label label) {
            String indicator = sortAscending ? " ▲" : " ▼";
            label.setText(label.getText() + indicator);
        }
    }

    /**
     * Настраивает контекстное меню для строк таблицы.
     */
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem(I18N.get("table.edit"));
        editItem.setOnAction(e -> {
            HumanBeing selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openEditDialog(selected);
        });

        MenuItem deleteItem = new MenuItem(I18N.get("table.remove"));
        deleteItem.setOnAction(e -> {
            HumanBeing selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) handleDelete(selected);
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        table.setContextMenu(contextMenu);

        // Обновляем тексты меню при смене локали
        I18N.localeProperty().addListener((obs, oldL, newL) -> {
            if (editItem != null) editItem.setText(I18N.get("table.edit"));
            if (deleteItem != null) deleteItem.setText(I18N.get("table.remove"));
        });
    }

    // ========================================================================
    // 🔹 Фильтрация и сортировка через Streams API
    // ========================================================================

    /**
     * Фильтрует данные таблицы с использованием Streams API.
     * Поиск осуществляется по всем текстовым и числовым полям объекта.
     * @param searchText строка поиска
     */
    private void filterTableWithStreams(String searchText) {
        List<HumanBeing> result;

        if (searchText == null || searchText.trim().isEmpty()) {
            // Если поиск пустой — показываем все данные
            result = new ArrayList<>(tableData);
        } else {
            String query = searchText.toLowerCase().trim();

            // ✅ STREAMS API: цепочка фильтрации
            result = tableData.stream()
                    .filter(h -> matchesSearchQuery(h, query))
                    .collect(Collectors.toList());
        }

        // Применяем текущую сортировку к отфильтрованным данным
        result = applyCurrentSort(result);

        // Обновляем таблицу
        table.setItems(FXCollections.observableArrayList(result));
    }

    /**
     * Проверяет, соответствует ли объект поисковому запросу.
     * Используется внутри filter() Streams API.
     */
    private boolean matchesSearchQuery(HumanBeing h, String query) {
        // Поиск по имени
        if (h.getName() != null && h.getName().toLowerCase().contains(query)) return true;

        // Поиск по владельцу
        if (h.getOwner() != null && h.getOwner().getLogin() != null
                && h.getOwner().getLogin().toLowerCase().contains(query)) return true;

        // Поиск по типу оружия
        if (h.getWeaponType() != null
                && h.getWeaponType().toString().toLowerCase().contains(query)) return true;

        // Поиск по настроению
        if (h.getMood() != null
                && h.getMood().toString().toLowerCase().contains(query)) return true;

        // Поиск по координатам (числовой поиск)
        if (h.getCoordinates() != null) {
            if (String.valueOf(h.getCoordinates().getX()).contains(query)) return true;
            if (String.valueOf(h.getCoordinates().getY()).contains(query)) return true;
        }

        // Поиск по статусу "реальный герой"
        if (String.valueOf(h.getRealHero()).toLowerCase().contains(query)) return true;

        // Поиск по дате создания
        if (h.getCreationDate() != null
                && h.getCreationDate().toString().toLowerCase().contains(query)) return true;

        // Поиск по ID
        if (String.valueOf(h.getId()).contains(query)) return true;

        return false;
    }

    /**
     * Применяет текущие параметры сортировки к списку через Streams API.
     */
    private List<HumanBeing> applyCurrentSort(List<HumanBeing> source) {
        Comparator<HumanBeing> comparator = getComparatorForField(sortField);
        if (!sortAscending) {
            comparator = comparator.reversed();
        }
        return source.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Возвращает Comparator для указанного поля через Streams API.
     */
    private Comparator<HumanBeing> getComparatorForField(String field) {
        return switch (field) {
            case "id" -> Comparator.comparing(HumanBeing::getId);

            case "name" -> Comparator.comparing(
                    h -> h.getName() != null ? h.getName() : "",
                    String.CASE_INSENSITIVE_ORDER
            );

            case "coordinates" -> Comparator.comparing(
                    h -> h.getCoordinates() != null ? h.getCoordinates().getX() : 0.0
            );

            case "creationDate" -> Comparator.comparing(
                    HumanBeing::getCreationDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );

            case "realHero" -> Comparator.comparing(HumanBeing::getRealHero);

            case "weaponType" -> Comparator.comparing(
                    h -> h.getWeaponType() != null ? h.getWeaponType().toString() : ""
            );

            case "mood" -> Comparator.comparing(
                    h -> h.getMood() != null ? h.getMood().toString() : ""
            );

            case "owner" -> Comparator.comparing(
                    h -> h.getOwner() != null && h.getOwner().getLogin() != null
                            ? h.getOwner().getLogin()
                            : ""
            );

            default -> Comparator.comparing(HumanBeing::getId);
        };
    }

    /**
     * Применяет сортировку к текущим данным таблицы через Streams API.
     * Вызывается при клике на заголовок колонки.
     */
    private void applySortWithStreams() {
        List<HumanBeing> currentList = new ArrayList<>(table.getItems());
        List<HumanBeing> sorted = applyCurrentSort(currentList);
        table.setItems(FXCollections.observableArrayList(sorted));
    }

    // ========================================================================
    // 🔹 Загрузка и синхронизация данных
    // ========================================================================

    /**
     * Загружает коллекцию объектов с сервера.
     * Выполняется в фоновом потоке, обновление UI через Platform.runLater.
     */
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
                        // Обновляем основной список данных
                        tableData.setAll(humans);

                        // Применяем текущий фильтр поиска если он есть
                        String searchText = searchField.getText();
                        if (searchText != null && !searchText.isEmpty()) {
                            filterTableWithStreams(searchText);
                        } else {
                            // Если поиска нет — показываем отсортированные данные
                            List<HumanBeing> sorted = applyCurrentSort(humans);
                            table.setItems(FXCollections.observableArrayList(sorted));
                        }

                        // Синхронизируем Canvas
                        if (canvasRenderer != null) {
                            canvasRenderer.syncData(humans);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError(I18N.get("msg.error.network", e.getMessage()))
                );
            }
        }).start();
    }

    // ========================================================================
    // 🔹 Обработка команд (меню / кнопки)
    // ========================================================================

    @FXML private void handleShow() { loadCollection(); }

    @FXML private void handleHelp() {
        sendCommandRequest(CommandType.HELP, new String[0], null, "cmd.help");
    }

    @FXML private void handleInfo() {
        sendCommandRequest(CommandType.INFO, new String[0], null, "cmd.info");
    }

    @FXML private void handleClear() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, I18N.get("msg.confirm.clear"));
        confirm.setTitle(I18N.get("window.title"));
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sendCommandRequest(CommandType.CLEAR, new String[0], null, "cmd.clear", this::loadCollection);
        }
    }

    @FXML private void handleSort() {
        sendCommandRequest(CommandType.SORT, new String[0], null, "cmd.sort", this::loadCollection);
    }

    @FXML private void handleSumOfImpactSpeed() {
        sendCommandRequest(CommandType.SUM_OF_IMPACT_SPEED, new String[0], null, "cmd.sum");
    }

    @FXML private void handlePrintFieldDescendingMood() {
        sendCommandRequest(CommandType.PRINT_FIELD_DESCENDING_MOOD, new String[0], null, "cmd.print.mood");
    }

    @FXML private void handleFilterContainsName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18N.get("window.title"));
        dialog.setHeaderText(I18N.get("cmd.filter.name"));
        dialog.setContentText(I18N.get("dialog.name") + ":");

        dialog.showAndWait().ifPresent(name ->
                sendCommandRequest(CommandType.FILTER_CONTAINS_NAME, new String[]{name}, null, "cmd.filter.name")
        );
    }

    @FXML private void handleRemoveAt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18N.get("window.title"));
        dialog.setHeaderText(I18N.get("cmd.remove.index"));
        dialog.setContentText("Index:");

        dialog.showAndWait().ifPresent(index ->
                sendCommandRequest(CommandType.REMOVE_AT, new String[]{index}, null, "cmd.remove.index", this::loadCollection)
        );
    }

    @FXML private void handleRemoveById() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18N.get("window.title"));
        dialog.setHeaderText(I18N.get("cmd.remove.id"));
        dialog.setContentText("ID:");

        dialog.showAndWait().ifPresent(id ->
                sendCommandRequest(CommandType.REMOVE_BY_ID, new String[]{id}, null, "cmd.remove.id", this::loadCollection)
        );
    }

    @FXML private void handleExecuteScript() {
        FileChooser fc = new FileChooser();
        fc.setTitle(I18N.get("cmd.execute"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Script Files", "*.txt"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            sendCommandRequest(CommandType.EXECUTE_SCRIPT, new String[]{file.getAbsolutePath()}, null, "cmd.execute");
        }
    }

    @FXML private void handleInsertAt() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(I18N.get("window.title"));
        dialog.setHeaderText(I18N.get("cmd.insert"));
        dialog.setContentText("Index:");

        dialog.showAndWait().ifPresent(index -> {
            HumanBeingDialog hbDialog = new HumanBeingDialog(null, currentUser);
            hbDialog.showAndWait().ifPresent(human ->
                    sendCommandRequest(CommandType.INSERT_AT, new String[]{index}, human, "cmd.insert", this::loadCollection)
            );
        });
    }

    @FXML private void handleAdd() {
        HumanBeingDialog dialog = new HumanBeingDialog(null, currentUser);
        dialog.showAndWait().ifPresent(newHuman ->
                sendCommandRequest(CommandType.ADD, new String[0], newHuman, "cmd.add", this::loadCollection)
        );
    }

    @FXML private void handleUpdate() {
        HumanBeing selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(I18N.get("table.edit"));
            return;
        }
        openEditDialog(selected);
    }

    @FXML private void handleRemove() {
        HumanBeing selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleDelete(selected);
        }
    }

    @FXML private void handleLogout() {
        // Останавливаем сервис синхронизации
        if (syncService != null) syncService.stop();

        // Возвращаемся на экран авторизации
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/gui/view/auth-view.fxml")
                    // ResourceBundle больше не передаём
            );
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Универсальный метод отправки команд на сервер.
     */
    private void sendCommandRequest(CommandType cmd, String[] args, Object data, String messageKey) {
        sendCommandRequest(cmd, args, data, messageKey, null);
    }

    private void sendCommandRequest(CommandType cmd, String[] args, Object data, String messageKey, Runnable onSuccess) {
        new Thread(() -> {
            try {
                java.io.Serializable serializableData = null;
                if (data instanceof java.io.Serializable) {
                    serializableData = (java.io.Serializable) data;
                }

                Request req = new Request(cmd, args, serializableData, currentUser);
                Response resp = networkClient.sendRequest(req);

                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        if (onSuccess != null) onSuccess.run();
                        else showAlert(I18N.get(messageKey), resp.getMessage());
                    } else {
                        showError(resp.getMessage());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError(I18N.get("msg.error.network", e.getMessage())));
            }
        }).start();
    }

    // ========================================================================
    // 🔹 Canvas: обработка кликов и визуализация
    // ========================================================================

    /**
     * Обработчик клика по объекту на Canvas.
     * Показывает информацию и предлагает редактирование для владельца.
     */
    private void handleCanvasClick(HumanBeing human) {
        if (human == null) return;

        // Показываем информацию об объекте
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("canvas.click.info"));
        alert.setHeaderText(human.getName() + " (ID: " + human.getId() + ")");

        StringBuilder content = new StringBuilder();
        content.append(I18N.get("table.owner")).append(": ").append(human.getOwner().getLogin()).append("\n");
        content.append(I18N.get("table.coordinates")).append(": ");
        if (human.getCoordinates() != null) {
            content.append("(")
                    .append(I18N.formatNumber(human.getCoordinates().getX())).append("; ")
                    .append(I18N.formatNumber(human.getCoordinates().getY()))
                    .append(")");
        }
        content.append("\n").append(I18N.get("table.creationDate")).append(": ")
                .append(human.getCreationDate() != null ? I18N.formatDate(human.getCreationDate()) : "");

        alert.setContentText(content.toString());
        alert.showAndWait();

        // Если текущий пользователь — владелец, предлагаем редактирование
        if (human.getOwner() != null && human.getOwner().getLogin().equals(currentUser.getLogin())) {
            openEditDialog(human);
        }
    }

    // ========================================================================
    // 🔹 Диалоги: добавление / редактирование
    // ========================================================================

    /**
     * Открывает диалог редактирования объекта.
     * Работает как для новых, так и для существующих объектов.
     */
    private void openEditDialog(HumanBeing human) {
        // Проверка прав: редактировать можно только свои объекты
        if (human.getId() != null && human.getOwner() != null
                && !human.getOwner().getLogin().equals(currentUser.getLogin())) {
            showError(I18N.get("msg.error.owner"));
            return;
        }

        HumanBeingDialog dialog = new HumanBeingDialog(human, currentUser);
        dialog.showAndWait().ifPresent(updated ->
                sendCommandRequest(CommandType.UPDATE, new String[]{String.valueOf(updated.getId())}, updated, "dialog.save", this::loadCollection)
        );
    }

    /**
     * Обрабатывает удаление объекта с подтверждением.
     */
    private void handleDelete(HumanBeing selected) {
        // Проверка прав на удаление
        if (selected.getOwner() == null || !selected.getOwner().getLogin().equals(currentUser.getLogin())) {
            showError(I18N.get("msg.error.owner"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(I18N.get("window.title"));
        confirm.setHeaderText(I18N.get("msg.confirm.delete", selected.getName()));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            sendCommandRequest(
                    CommandType.REMOVE_BY_ID,
                    new String[]{String.valueOf(selected.getId())},
                    null,
                    "msg.success.deleted",
                    this::loadCollection
            );
        }
    }

    // ========================================================================
    // 🔹 Утилиты: отображение сообщений
    // ========================================================================

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18N.get("window.title"));
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18N.get("error.validation.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}