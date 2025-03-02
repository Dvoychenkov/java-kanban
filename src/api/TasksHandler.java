package api;

import com.sun.net.httpserver.HttpExchange;
import entities.Task;
import enums.HttpMethod;
import exceptions.NotFoundException;
import exceptions.TaskIntersectionException;
import interfaces.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static enums.HttpStatusCode.*;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (HttpMethod.valueOf(exchange.getRequestMethod())) {
                case GET -> handleGetTasksOrTaskById(exchange);
                case POST -> handleCreateOrUpdateTask(exchange);
                case DELETE -> handleDeleteTask(exchange);
                default -> exchange.sendResponseHeaders(METHOD_NOT_ALLOWED.code(), -1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // В зависимости от длины пути извлекаем все задачи или задачу по id
    private void handleGetTasksOrTaskById(HttpExchange exchange) throws IOException {
        if (getPathLengthOfRequest(exchange) == 2) {
            List<Task> tasks = taskManager.getTasks();
            sendText(exchange, gson.toJson(tasks), OK.code());
            return;
        }

        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                Task task = taskManager.getTask(id.get());
                sendText(exchange, gson.toJson(task), OK.code());
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

    // Если указан id - обновляем задачу, иначе добавляем новую
    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), DEFAULT_CHARSET)) {
            Task task = gson.fromJson(reader, Task.class);

            if (task.getId() == 0) {
                taskManager.addNewTask(task);
            } else {
                taskManager.updateTask(task);
            }

            sendText(exchange, null, CREATED.code());
        } catch (TaskIntersectionException ex) {
            sendHasIntersections(exchange);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // Удаляем задачу
    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                taskManager.deleteTaskById(id.get());
                sendText(exchange, null, OK.code());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }
}