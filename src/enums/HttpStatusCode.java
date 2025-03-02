package enums;

public enum HttpStatusCode {
//    OK(200, "OK"),
//    NOT_FOUND(404, "Not Found");

//    private final int value;
//    private final String reason;

//    HttpStatusCode(int value, String reason) {
//        this.value = value;
//        this.reason = reason;
//    }

//    public static HttpStatusCode getByValue(int value) {
//        // Реализуйте логику поиска статуса по числовому значению
//    }

//    @Override
//    public String toString() {
//        return this.value + " " + this.reason; // Вывод статуса
//    }

    OK(200),
    CREATED(201),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpStatusCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}