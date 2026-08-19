package todo;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.tinylog.Logger;
import javafx.scene.control.Button;

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
                showError("Nem sikerült megnyitni a fájlt.");
            }
        }
    }

    @FXML
    private void onSave() {
        if (todoModel.getFilePath() != null) {
            Logger.debug("Saving file");
            try {
                todoModel.save();
            } catch (IOException e) {
                Logger.error(e, "Failed to save file");
                showError("Nem sikerült menteni.");
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
            Logger.debug("Saving file as {}", file);
            try {
                todoModel.saveAs(file.getPath());
            } catch (IOException e) {
                Logger.error(e, "Failed to save file");
                showError("Nem sikerült menteni a fájlt.");
            }
        }
    }

    private void showError(String msg) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private Window getWindow() {
        return tasksContainer.getScene().getWindow();
    }
}