package api;

import com.sun.net.httpserver.HttpExchange;
import entities.Task;
import enums.HttpMethod;
import interfaces.TaskManager;

import java.io.IOException;
import java.util.List;

import static enums.HttpStatusCode.*;
import static enums.HttpMethod.*;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpMethod.valueOf(exchange.getRequestMethod()) == GET) {
                handleGetHistory(exchange);
            } else {
                exchange.sendResponseHeaders(METHOD_NOT_ALLOWED.code(), -1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendText(exchange, gson.toJson(history), OK.code());
    }
}
