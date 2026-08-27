package todo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeSpinner extends Spinner<LocalTime> {

    private enum Mode {
        HOURS, MINUTES
    }

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.HOURS);

    public TimeSpinner(LocalTime time) {
        setEditable(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        StringConverter<LocalTime> converter = new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime localTime) {
                return localTime == null ? "" : formatter.format(localTime);
            }

            @Override
            public LocalTime fromString(String string) {
                if(string == null || string.isEmpty()) {
                    return null;
                }

                try {
                    return LocalTime.parse(string, formatter);
                } catch (Exception e) {
                    return null;
                }
            }
        };

        TextFormatter<LocalTime> textFormatter = new TextFormatter<>(converter, time);
        getEditor().setTextFormatter(textFormatter);

        getEditor().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if(!isNowFocused) {
                commitEditorValue();
            }
        });

        getEditor().setOnAction(event -> {
            commitEditorValue();
        });

        setValueFactory(new SpinnerValueFactory<LocalTime>() {

            {
                setValue(time);
            }

            @Override
            public void decrement(int steps) {
                LocalTime current = getValue();
                LocalTime newValue = incrementMode(current, -steps);
                setValue(newValue);
                selectModeInEditor();
            }

            @Override
            public void increment(int steps) {
                LocalTime current = getValue();
                LocalTime newValue = incrementMode(current, steps);
                setValue(newValue);
                selectModeInEditor();
            }
        });

        getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.contains(":")) {
                int caret = getEditor().getCaretPosition();
                int colonIndex = newText.indexOf(':');
                mode.set(caret > colonIndex ? Mode.MINUTES : Mode.HOURS);
            }
        });

    }
    private LocalTime incrementMode(LocalTime time, int steps) {
        if (time == null) {
            time = LocalTime.MIDNIGHT;
        }

        if (mode.get() == Mode.HOURS) {
            return time.plusHours(steps);
        } else {
            return time.plusMinutes(steps);
        }
    }

    private void selectModeInEditor() {
        String text = getEditor().getText();
        if (text == null || !text.contains(":")) return;

        int colonIndex = text.indexOf(':');
        if (mode.get() == Mode.HOURS) {
            getEditor().selectRange(0, colonIndex);
        } else {
            getEditor().selectRange(colonIndex + 1, text.length());
        }
    }

    private void commitEditorValue() {
        String text = getEditor().getText();

        if (text == null || text.isBlank()) {
            getValueFactory().setValue(null);
            return;
        }

        try {
            LocalTime time = LocalTime.parse(
                    text,
                    DateTimeFormatter.ofPattern("HH:mm")
            );

            getValueFactory().setValue(time);
        } catch (Exception e) {
            LocalTime current = getValue();

            if (current != null) {
                getEditor().setText(
                        current.format(DateTimeFormatter.ofPattern("HH:mm"))
                );
            } else {
                getEditor().clear();
            }
        }
    }
}
