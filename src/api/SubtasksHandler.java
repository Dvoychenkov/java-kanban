package api;

import com.sun.net.httpserver.HttpExchange;
import entities.Subtask;
import enums.HttpMethod;
import exceptions.NotFoundException;
import exceptions.TaskIntersectionException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static enums.HttpStatusCode.*;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(HttpTaskServer httpTaskServer) {
        super(httpTaskServer);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (HttpMethod.valueOf(exchange.getRequestMethod())) {
                case GET -> handleGetSubtasksOrSubtaskById(exchange);
                case POST -> handleCreateOrUpdateSubtask(exchange);
                case DELETE -> handleDeleteSubtask(exchange);
                default -> exchange.sendResponseHeaders(METHOD_NOT_ALLOWED.code(), -1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // В зависимости от длины пути извлекаем все подзадачи или подзадачу по id
    private void handleGetSubtasksOrSubtaskById(HttpExchange exchange) throws IOException {
        if (getPathLengthOfRequest(exchange) == 2) {
            List<Subtask> tasks = taskManager.getSubtasks();
            sendText(exchange, gson.toJson(tasks), OK.code());
            return;
        }

        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                Subtask subtask = taskManager.getSubtask(id.get());
                sendText(exchange, gson.toJson(subtask), OK.code());
            } else {
                sendNotFound(exchange);
            }
        } catch (NotFoundException ex) {
            sendNotFound(exchange);
        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // Если указан id - обновляем подзадачу, иначе добавляем новую
    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), DEFAULT_CHARSET)) {
            Subtask subtask = gson.fromJson(reader, Subtask.class);

            if (subtask.getId() == 0) {
                taskManager.addNewSubtask(subtask);
            } else {
                taskManager.updateSubtask(subtask);
            }

            sendText(exchange, null, CREATED.code());
        } catch (TaskIntersectionException ex) {
            sendHasIntersections(exchange);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // Удаляем подзадачу
    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                taskManager.deleteSubtaskById(id.get());
                sendText(exchange, null, OK.code());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }
}
