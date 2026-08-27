package todo;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoItem {
    private final BooleanProperty done = new SimpleBooleanProperty(false);
    private final StringProperty text = new SimpleStringProperty("");
    private final ObjectProperty<LocalDateTime> dueDateTime = new SimpleObjectProperty<>(null);

    public TodoItem() {}

    public TodoItem(boolean done, String text, LocalDateTime dueDateTime) {
        this.done.set(done);
        this.text.set(text);
        this.dueDateTime.set(dueDateTime);
    }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean done) { this.done.set(done); }
    public BooleanProperty doneProperty() { return done; }

    public String getText() { return text.get(); }
    public void setText(String text) { this.text.set(text); }
    public StringProperty textProperty() { return text; }

    public LocalDateTime getDueDateTime() {
        return dueDateTime.get();
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime.set(dueDateTime);
    }
    public ObjectProperty<LocalDateTime> dueDateTimeProperty() {
        return dueDateTime;
    }
}
