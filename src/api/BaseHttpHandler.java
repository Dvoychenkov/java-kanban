package api;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static enums.HttpStatusCode.*;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    protected static final String PATH_DELIMITER = "/";
    protected static final int ID_PATH_INDEX = 2;
    protected String[] pathParts;

    BaseHttpHandler(HttpTaskServer httpTaskServer) {
        this.taskManager = httpTaskServer.getTaskManager();
        this.gson = httpTaskServer.getGson();
    }

    protected void sendText(HttpExchange httpExchange, String text, int statusCode) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(statusCode, 0);
        try (OutputStream os = httpExchange.getResponseBody()) {
            text = (text == null) ? "" : text;
            os.write(text.getBytes(DEFAULT_CHARSET));
        }
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(Map.of("error", "Entity not found")), NOT_FOUND.code());
    }

    protected void sendHasIntersections(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(Map.of("error", "Entity has time conflict")), NOT_ACCEPTABLE.code());
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(Map.of("error", "Internal Server Error")), INTERNAL_SERVER_ERROR.code());
    }

    private void splitPathByParts(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        pathParts = path.split(PATH_DELIMITER);
    }

    protected int getPathLengthOfRequest(HttpExchange exchange) {
        splitPathByParts(exchange);
        return pathParts.length;
    }

    protected Optional<Integer> extractIdFromRequest(HttpExchange exchange) {
        splitPathByParts(exchange);
        if (pathParts.length < ID_PATH_INDEX + 1 || pathParts[ID_PATH_INDEX].isBlank()) {
            return Optional.empty();
        }
        int id = Integer.parseInt(pathParts[ID_PATH_INDEX]);
        return Optional.of(id);
    }
}
