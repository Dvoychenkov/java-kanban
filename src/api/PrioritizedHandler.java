package api;

import com.sun.net.httpserver.HttpExchange;
import entities.Task;
import enums.HttpMethod;

import java.io.IOException;
import java.util.List;

import static enums.HttpMethod.GET;
import static enums.HttpStatusCode.METHOD_NOT_ALLOWED;
import static enums.HttpStatusCode.OK;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(HttpTaskServer httpTaskServer) {
        super(httpTaskServer);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (HttpMethod.valueOf(exchange.getRequestMethod()) == GET) {
                handleGetPrioritizedTasks(exchange);
            } else {
                exchange.sendResponseHeaders(METHOD_NOT_ALLOWED.code(), -1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendText(exchange, gson.toJson(prioritizedTasks), OK.code());
    }
}
