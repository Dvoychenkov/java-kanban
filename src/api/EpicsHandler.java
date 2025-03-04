package api;

import com.sun.net.httpserver.HttpExchange;
import entities.Epic;
import entities.Subtask;
import enums.HttpMethod;
import exceptions.NotFoundException;
import exceptions.TaskIntersectionException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static enums.HttpStatusCode.*;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(HttpTaskServer httpTaskServer) {
        super(httpTaskServer);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (HttpMethod.valueOf(exchange.getRequestMethod())) {
                case GET -> handleGetEpicsOrEpicByIdOrEpicSubtasks(exchange);
                case POST -> handleCreateOrUpdateEpic(exchange);
                case DELETE -> handleDeleteEpic(exchange);
                default -> exchange.sendResponseHeaders(METHOD_NOT_ALLOWED.code(), -1);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // В зависимости от длины пути извлекаем все эпики или эпик по id или подзадачи эпика
    private void handleGetEpicsOrEpicByIdOrEpicSubtasks(HttpExchange exchange) throws IOException {
        if (getPathLengthOfRequest(exchange) == 2) {
            List<Epic> epics = taskManager.getEpics();
            sendText(exchange, gson.toJson(epics), OK.code());
            return;
        }

        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                // Дополнительная логика обработки по проверке, пытаемся мы получить подзадачи или эпик
                if (getPathLengthOfRequest(exchange) < 4) {
                    Epic epic = taskManager.getEpicById(id.get());
                    sendText(exchange, gson.toJson(epic), OK.code());
                    return;
                }
                if (!pathParts[3].equals("subtasks")) {
                    sendNotFound(exchange);
                }
                List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(id.get());
                sendText(exchange, gson.toJson(epicSubtasks), OK.code());
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

    // Если указан id - обновляем эпик, иначе добавляем новый (в ТЗ обновления нет, но для порядка добавлено)
    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), DEFAULT_CHARSET)) {
            // Оборачиваем в создание через конструктор для корректного ведения его полей
            Epic epic = new Epic(gson.fromJson(reader, Epic.class));

            if (epic.getId() == 0) {
                taskManager.createEpic(epic);
            } else {
                taskManager.updateEpic(epic);
            }

            sendText(exchange, null, CREATED.code());
        } catch (TaskIntersectionException ex) {
            sendHasIntersections(exchange);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }

    // Удаляем эпик
    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        try {
            Optional<Integer> id = extractIdFromRequest(exchange);
            if (id.isPresent()) {
                taskManager.deleteEpic(id.get());
                sendText(exchange, null, OK.code());
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            sendServerError(exchange);
        }
    }
}
