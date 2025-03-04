package exceptions;

public class TaskIntersectionException extends RuntimeException {
    public TaskIntersectionException(String message) {
        super(message);
    }

    public TaskIntersectionException(Exception ex) {
        super(ex);
    }
}