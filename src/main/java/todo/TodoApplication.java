package todo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TodoApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/to-do-editor.fxml"));
        stage.setTitle("JavaFX To-Do List");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }
}