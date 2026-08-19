package todo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TodoItem {
    private final BooleanProperty done = new SimpleBooleanProperty(false);
    private final StringProperty text = new SimpleStringProperty("");

    public TodoItem() {}

    public TodoItem(boolean done, String text) {
        this.done.set(done);
        this.text.set(text);
    }

    public boolean isDone() { return done.get(); }
    public void setDone(boolean done) { this.done.set(done); }
    public BooleanProperty doneProperty() { return done; }

    public String getText() { return text.get(); }
    public void setText(String text) { this.text.set(text); }
    public StringProperty textProperty() { return text; }
}