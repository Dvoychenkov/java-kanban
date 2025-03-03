package api;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import exceptions.NotFoundException;
import interfaces.TaskManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static enums.HttpStatusCode.*;
import static org.junit.jupiter.api.Assertions.*;

abstract public class HttpTaskManagerEpicsTest<T extends TaskManager> extends HttpTaskServerTest<T> {

    HttpTaskManagerEpicsTest() {
        super();
        baseUrl = baseUrl + "/epics/";
    }

    @Test
    public void shouldAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при добавлении эпика");

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals(epic.getTitle(), epicsFromManager.getFirst().getTitle(), "Некорректный заголовок эпика");
    }

    @Test
    public void shouldGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        Epic epic2 = new Epic("Epic 2", "Epic 2 Description", TaskStatus.NEW);
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении эпиков");

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(epics, "Список эпиков не должен быть null");
        assertEquals(2, epics.length, "Некорректное количество эпиков");
        assertEquals(epic1.getTitle(), epics[0].getTitle(), "Некорректный заголовок первого эпика");
        assertEquals(epic2.getTitle(), epics[1].getTitle(), "Некорректный заголовок второго эпика");
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        int epicId = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении эпика");

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(receivedEpic, "Эпик не должна быть null");
        assertEquals(epicId, receivedEpic.getId(), "ID эпика не совпадает");
        assertEquals(epic.getTitle(), receivedEpic.getTitle(), "Имя эпика не совпадает");
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        int epicId = manager.createEpic(epic);

        Epic updatedEpic = new Epic("Epic 1 updated", "Epic 1 Description updated", TaskStatus.NEW);
        updatedEpic.setId(epicId);
        String updatedEpicJson = gson.toJson(updatedEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при обновлении эпика");

        Epic storedEpic = manager.getEpicById(epicId);
        assertNotNull(storedEpic, "Эпик должна существовать");
        assertEquals(updatedEpic.getTitle(), storedEpic.getTitle(), "Имя эпика не обновилось");
        assertEquals(updatedEpic.getDescription(), storedEpic.getDescription(), "Описание эпика не обновилось");
    }

    @Test
    public void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        int epicId = manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + epicId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при удалении эпика");

        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class,
                () -> manager.getEpicById(epicId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найден"),
                "Ожидалось исключение о том, что эпик не найден");
    }

    @Test
    public void shouldReturn404IfEpicNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + 999))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_FOUND.code(), response.statusCode(),
                "Некорректный статус ответа для несуществующего эпика");
    }

    @Test
    public void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic 1 Description", TaskStatus.NEW);
        int epicId = manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epicId,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.IN_PROGRESS, epicId,
                LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(15));
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + epicId + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении подзадач эпика");

        Task[] epicTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(epicTasks, "Список задач эпика не должен быть null");

        assertNotNull(epicTasks, "Список подзадач эпика не должен быть null");
        assertEquals(2, epicTasks.length, "Некорректное количество подзадач эпика");
        assertEquals(subtask1.getTitle(), epicTasks[0].getTitle(), "Некорректный заголовок первой подзадачи эпика");
        assertEquals(subtask2.getTitle(), epicTasks[1].getTitle(), "Некорректный заголовок второй подзадачи эпика");
    }

    @Test
    public void shouldReturn404IfEpicNotFoundWhileGettingSubtasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + 999 + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_FOUND.code(), response.statusCode(),
                "Некорректный статус ответа для несуществующего эпика при получении его подзадач");
    }
}
