package exceptions;

public class TaskStringParseException extends RuntimeException {
    public TaskStringParseException(String message) {
        super(message);
    }

    public TaskStringParseException(Exception ex) {
        super(ex);
    }
}