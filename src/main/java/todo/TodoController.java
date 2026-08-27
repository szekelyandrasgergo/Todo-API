package todo;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TodoController {

    private static final String DEFAULT_TITLE = "Unnamed Todo List";
    private static final String MODIFIED_INDICATOR = "*";

    @FXML
    private VBox tasksContainer;

    private final TodoModel todoModel = new TodoModel();

    private final Map<TodoItem, HBox> taskRows = new HashMap<>();

    @FXML
    private void initialize() {

        Platform.runLater(() -> ((Stage) tasksContainer.getScene().getWindow()).titleProperty().bind(
                Bindings.when(todoModel.filePathProperty().isNotNull())
                        .then(todoModel.filePathProperty())
                        .otherwise(DEFAULT_TITLE)
                        .concat(Bindings.when(todoModel.modifiedProperty())
                                .then(MODIFIED_INDICATOR)
                                .otherwise("")))
        );

        todoModel.getTasks().addListener(
                (ListChangeListener<TodoItem>) change -> refreshTasks()
        );

    }

    private HBox createTaskRow(TodoItem item) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 5;");

        CheckBox checkBox = new CheckBox();
        TextField textField = new TextField();
        Button deleteButton = new Button("🗑");
        DatePicker datePicker = new DatePicker();
        TimeSpinner timeSpinner = new TimeSpinner(null);

        if (item.getDueDateTime() != null) {
            datePicker.setValue(item.getDueDateTime().toLocalDate());
            timeSpinner.getValueFactory().setValue(item.getDueDateTime().toLocalTime());
        }

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        checkBox.selectedProperty().bindBidirectional(item.doneProperty());
        textField.textProperty().bindBidirectional(item.textProperty());

        textField.styleProperty().bind(Bindings.when(item.doneProperty())
                .then("-fx-strikethrough: true; -fx-text-fill: gray;")
                .otherwise("-fx-strikethrough: false; -fx-text-fill: black;"));

        deleteButton.setOnAction(event -> {
            todoModel.getTasks().remove(item);
        });

        Runnable updateDueDate = () -> {
            LocalDate date = datePicker.getValue();
            LocalTime time = timeSpinner.getValue();

            if (date == null || time == null) {
                item.setDueDateTime(null);
                return;
            }

            LocalDateTime selectedDateTime = LocalDateTime.of(date, time);

            // Ha a kiválasztott időpont a jelenlegi pillanatnál korábbi
            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                // Korrigáljuk az aktuális időpontra (vagy visszaállíthatod az előző érvényes értékre is)
                selectedDateTime = LocalDateTime.now();

                datePicker.setValue(selectedDateTime.toLocalDate());
                timeSpinner.getValueFactory().setValue(selectedDateTime.toLocalTime());
            }

            item.setDueDateTime(selectedDateTime);
        };

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDueDate.run());
        timeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateDueDate.run());

        timeSpinner.getEditor().focusedProperty().addListener((obs, oldVal, isNowFocused) -> {
            if (!isNowFocused) {
                updateDueDate.run();
            }
        });

        row.getChildren().addAll(checkBox, textField, datePicker, timeSpinner, deleteButton);
        return row;
    }

    @FXML
    private void onAbout() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Javafx to-do api");
        alert.setContentText("""
        A lightweight task manager built with JavaFX.
        
        Features:
        - Add, complete, and delete tasks
        - Save and open task lists from files
        - Automatic cleanup of completed tasks upon saving
        
        Created by: András Gergő Székely
        Version: 1.1.0
        """);
        alert.showAndWait();
    }

    @FXML
    private void onNew() {
        todoModel.reset();
    }

    @FXML
    private void onAddTask() {
        for (TodoItem item : todoModel.getTasks()) {
            if (item.getText() == null || item.getText().isBlank()) {
                showWarning("Empty Task",
                        "There is already an incomplete task. Please complete or remove it before adding a new one.");
                return;
            }
        }
        todoModel.addTask();
    }

    @FXML
    private void onOpen() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Open Todo List");
        var file = fileChooser.showOpenDialog(getWindow());
        if (file != null) {
            Logger.debug("Opening file {}", file);
            try {
                todoModel.open(file.getPath());
            } catch (IOException e) {
                Logger.error(e, "Failed to open file");
                showError("Failed to open file");
            }
        }
    }

    @FXML
    private void onSave() {
        tasksContainer.requestFocus();

        if (todoModel.getFilePath() != null) {
            if (!checkForEmptyTasksAndSave()) {
                return;
            }

            Logger.debug("Saving file");
            try {
                todoModel.save();
                refreshTasks();
            } catch (IOException e) {
                Logger.error(e, "Failed to save file");
                showError("Failed to save file.");
            }
        } else {
            performSaveAs();
        }
    }

    @FXML
    private void onSaveAs() {
        performSaveAs();
    }

    private void performSaveAs() {
        tasksContainer.requestFocus();
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Save Todo List As");
        var file = fileChooser.showSaveDialog(getWindow());

        if (file != null) {
            if (!checkForEmptyTasksAndSave()) {
                return;
            }

            Logger.debug("Saving file as {}", file);
            try {
                todoModel.saveAs(file.getPath());
                refreshTasks();
            } catch (IOException e) {
                Logger.error(e, "Failed to save file");
                showError("Failed to save file");
            }
        }
    }

    private boolean checkForEmptyTasksAndSave() {
        boolean hasEmptyTask = false;
        for (TodoItem item : todoModel.getTasks()) {
            if (item.getText() == null || item.getText().isBlank()) {
                hasEmptyTask = true;
                break;
            }
        }

        if (hasEmptyTask) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Empty Tasks Found");
            alert.setHeaderText("You have empty tasks in your list.");
            alert.setContentText("Would you like to automatically remove the empty rows before saving?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                todoModel.getTasks().removeIf(item -> item.getText() == null || item.getText().isBlank());
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private void showError(String msg) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showWarning(String title, String msg) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Window getWindow() {
        return tasksContainer.getScene().getWindow();
    }

    private void refreshTasks() {
        tasksContainer.getChildren().clear();
        taskRows.clear();

        for (TodoItem item : todoModel.getTasks()) {
            HBox row = createTaskRow(item);

            taskRows.put(item, row);
            tasksContainer.getChildren().add(row);
        }
    }
}