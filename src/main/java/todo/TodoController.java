package todo;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.tinylog.Logger;

import java.io.IOException;

public class TodoController {

    private static final String DEFAULT_TITLE = "Unnamed Todo List";
    private static final String MODIFIED_INDICATOR = "*";

    @FXML
    private VBox tasksContainer;

    private final TodoModel todoModel = new TodoModel();

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

        todoModel.tasksProperty().addListener((observable, oldValue, newValue) -> {
            tasksContainer.getChildren().clear();
            for (TodoItem item : newValue) {
                tasksContainer.getChildren().add(createTaskRow(item));
            }
        });
    }

    private HBox createTaskRow(TodoItem item) {
        HBox row = new HBox(10);
        row.setStyle("-fx-padding: 5;");

        CheckBox checkBox = new CheckBox();
        TextField textField = new TextField();
        Button deleteButton = new Button("🗑");

        deleteButton.setOnAction(event -> {
            todoModel.getTasks().remove(item);
        });

        checkBox.selectedProperty().bindBidirectional(item.doneProperty());
        textField.textProperty().bindBidirectional(item.textProperty());

        textField.styleProperty().bind(Bindings.when(item.doneProperty())
                .then("-fx-strikethrough: true; -fx-text-fill: gray;")
                .otherwise("-fx-strikethrough: false; -fx-text-fill: black;"));

        row.getChildren().addAll(checkBox, textField, deleteButton);
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
        if (todoModel.getFilePath() != null) {
            if (!checkForEmptyTasksAndSave()) {
                return;
            }

            Logger.debug("Saving file");
            try {
                todoModel.save();
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
}