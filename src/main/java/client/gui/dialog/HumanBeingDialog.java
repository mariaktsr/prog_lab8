package client.gui.dialog;

import client.gui.util.I18N;
import common.model.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Диалоговое окно для создания или редактирования объекта HumanBeing.
 */
public class HumanBeingDialog extends Dialog<HumanBeing> {

    // Поля формы
    private TextField nameField;
    private TextField xField;
    private TextField yField;
    private CheckBox realHeroCheck;
    private CheckBox hasToothpickCheck;
    private TextField impactSpeedField;
    private ChoiceBox<WeaponType> weaponChoice;
    private ChoiceBox<Mood> moodChoice;
    private CheckBox hasCarCheck;
    private TextField carNameField;

    private User owner;

    public HumanBeingDialog(HumanBeing humanToEdit, User owner) {
        this.owner = owner;
        setTitle(humanToEdit == null ? I18N.get("dialog.add.title") : I18N.get("dialog.edit.title"));
        setHeaderText(humanToEdit == null
                ? I18N.get("dialog.add.header")
                : I18N.get("dialog.edit.header", humanToEdit.getName()));

        // Кнопки
        ButtonType saveButtonType = new ButtonType(I18N.get("dialog.save"), ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Сетка формы
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Инициализация полей
        nameField = new TextField(humanToEdit != null ? humanToEdit.getName() : "");
        nameField.setPromptText(I18N.get("dialog.name"));

        xField = new TextField(humanToEdit != null ? String.valueOf(humanToEdit.getCoordinates().getX()) : "0");
        xField.setPromptText("X (double)");

        yField = new TextField(humanToEdit != null ? String.valueOf(humanToEdit.getCoordinates().getY()) : "0");
        yField.setPromptText("Y (> -228)");

        realHeroCheck = new CheckBox(I18N.get("dialog.realHero"));
        realHeroCheck.setSelected(humanToEdit != null && humanToEdit.getRealHero());

        hasToothpickCheck = new CheckBox(I18N.get("dialog.hasToothpick"));
        hasToothpickCheck.setSelected(humanToEdit != null && humanToEdit.isHasToothpick());

        impactSpeedField = new TextField(humanToEdit != null ? String.valueOf(humanToEdit.getImpactSpeed()) : "0");
        impactSpeedField.setPromptText("Speed (> -428)");

        weaponChoice = new ChoiceBox<>();
        weaponChoice.getItems().addAll(WeaponType.values());
        weaponChoice.setValue(humanToEdit != null ? humanToEdit.getWeaponType() : WeaponType.BAT);

        moodChoice = new ChoiceBox<>();
        moodChoice.getItems().addAll(Mood.values());
        moodChoice.setValue(humanToEdit != null ? humanToEdit.getMood() : Mood.CALM);

        hasCarCheck = new CheckBox(I18N.get("dialog.hasCar"));
        carNameField = new TextField();

        if (humanToEdit != null && humanToEdit.getCar() != null) {
            hasCarCheck.setSelected(true);
            carNameField.setText(humanToEdit.getCar().getName());
            carNameField.setDisable(false);
        } else {
            hasCarCheck.setSelected(false);
            carNameField.setDisable(true);
        }

        hasCarCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            carNameField.setDisable(!newVal);
            if (!newVal) carNameField.clear();
        });

        // Добавление в сетку
        int row = 0;
        grid.add(new Label(I18N.get("dialog.name")), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("X:"), 0, row);
        grid.add(xField, 1, row++);

        grid.add(new Label("Y:"), 0, row);
        grid.add(yField, 1, row++);

        grid.add(realHeroCheck, 0, row++, 2, 1);
        GridPane.setHalignment(realHeroCheck, HPos.LEFT);

        grid.add(hasToothpickCheck, 0, row++, 2, 1);
        GridPane.setHalignment(hasToothpickCheck, HPos.LEFT);

        grid.add(new Label(I18N.get("dialog.impactSpeed")), 0, row);
        grid.add(impactSpeedField, 1, row++);

        grid.add(new Label(I18N.get("dialog.weaponType")), 0, row);
        grid.add(weaponChoice, 1, row++);

        grid.add(new Label(I18N.get("dialog.mood")), 0, row);
        grid.add(moodChoice, 1, row++);

        grid.add(hasCarCheck, 0, row++, 2, 1);
        GridPane.setHalignment(hasCarCheck, HPos.LEFT);

        grid.add(new Label(I18N.get("dialog.carName")), 0, row);
        grid.add(carNameField, 1, row++);

        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(xField, Priority.ALWAYS);
        GridPane.setHgrow(yField, Priority.ALWAYS);
        GridPane.setHgrow(impactSpeedField, Priority.ALWAYS);
        GridPane.setHgrow(carNameField, Priority.ALWAYS);

        getDialogPane().setContent(grid);

        // Обработка нажатия "Сохранить"
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    double x = Double.parseDouble(xField.getText().trim());
                    Long y = Long.parseLong(yField.getText().trim());
                    Long speed = Long.parseLong(impactSpeedField.getText().trim());
                    WeaponType weapon = weaponChoice.getValue();
                    Mood mood = moodChoice.getValue();

                    Coordinates coords = new Coordinates(x, y);

                    Car car = null;
                    if (hasCarCheck.isSelected()) {
                        String cName = carNameField.getText().trim();
                        car = new Car(cName.isEmpty() ? null : cName);
                    }

                    if (humanToEdit == null) {
                        // Создание нового
                        return new HumanBeing(name, coords, realHeroCheck.isSelected(),
                                hasToothpickCheck.isSelected(), speed,
                                weapon, mood, car, owner);
                    } else {
                        // Редактирование существующего
                        return new HumanBeing(humanToEdit.getId(), name, coords, humanToEdit.getCreationDate(),
                                realHeroCheck.isSelected(), hasToothpickCheck.isSelected(),
                                speed, weapon, mood, car, owner);
                    }
                } catch (NumberFormatException e) {
                    showError(I18N.get("error.validation.number"));
                } catch (IllegalArgumentException e) {
                    showError(e.getMessage());
                }
            }
            return null;
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18N.get("error.validation.title"));
        alert.setHeaderText(I18N.get("error.validation.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }
}