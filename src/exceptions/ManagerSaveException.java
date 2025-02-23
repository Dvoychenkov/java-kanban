package exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }
    public ManagerSaveException(Exception ex) {
        super(ex);
    }
}
