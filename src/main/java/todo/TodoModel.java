package todo;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TodoModel {

    private final ReadOnlyStringWrapper filePath = new ReadOnlyStringWrapper(null);

    private final ListProperty<TodoItem> tasks = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper(false);

    public TodoModel() {
        tasks.addListener((observable, oldValue, newValue) -> {
            modified.set(true);
            System.out.println("Task list changed");
        });
    }

    public ReadOnlyStringProperty filePathProperty() {
        return filePath.getReadOnlyProperty();
    }
    public final String getFilePath() {
        return filePath.get();
    }
    public ListProperty<TodoItem> tasksProperty() {
        return tasks;
    }
    public final List<TodoItem> getTasks() {
        return tasks.get();
    }
    public ReadOnlyBooleanProperty modifiedProperty() {
        return modified.getReadOnlyProperty();
    }
    public final boolean isModified() {
        return modified.get();
    }

    public void addTask() {
        tasks.add(new TodoItem());
    }

    public void reset() {
        filePath.set(null);
        tasks.clear();
        modified.set(false);
    }

    public void open(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        List<TodoItem> loadedTasks = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                boolean done = Boolean.parseBoolean(parts[0]);
                String text = parts[1];
                loadedTasks.add(new TodoItem(done, text));
            }
        }

        this.filePath.set(filePath);
        tasks.setAll(loadedTasks);
        modified.set(false);
    }

    public void save() throws IOException {
        if (filePath.get() == null) {
            throw new IllegalStateException();
        }
        saveAs(filePath.get());
    }

    public void saveAs(String filePath) throws IOException {
        tasks.removeIf(TodoItem::isDone);

        List<String> lines = tasks.stream()
                .map(item -> item.isDone() + "|" + item.getText())
                .collect(Collectors.toList());

        Files.write(Path.of(filePath), lines);
        this.filePath.set(filePath);
        modified.set(false);
    }
}