package api;

import entities.Epic;
import entities.Subtask;
import enums.TaskStatus;
import exceptions.NotFoundException;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
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

abstract class HttpTaskManagerSubtasksTest<T extends TaskManager> extends HttpTaskServerTest<T> {
    Epic epic;

    HttpTaskManagerSubtasksTest() {
        super();
        baseUrl = baseUrl + "/subtasks/";
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        super.beforeEach();

        epic = new Epic("Epic title", "Epic description", TaskStatus.NEW);
        int epicId = manager.createEpic(epic);
        epic = manager.getEpicById(epicId);
    }

    @Test
    public void shouldAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при добавлении подзадачи");

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals(subtask.getTitle(), subtasksFromManager.getFirst().getTitle(), "Некорректный заголовок подзадачи");
    }

    @Test
    public void shouldGetAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(10));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.IN_PROGRESS, epic.getId(),
                LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(15));
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении подзадач");

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.length, "Некорректное количество подзадач");
        assertEquals(subtask1.getTitle(), subtasks[0].getTitle(), "Некорректный заголовок первой подзадачи");
        assertEquals(subtask2.getTitle(), subtasks[1].getTitle(), "Некорректный заголовок второй подзадачи");
    }

    @Test
    public void shouldGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        int subtaskId = manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + subtaskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении подзадачи");

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(receivedSubtask, "Подзадача не должна быть null");
        assertEquals(subtaskId, receivedSubtask.getId(), "ID подзадачи не совпадает");
        assertEquals(subtask.getTitle(), receivedSubtask.getTitle(), "Имя подзадачи не совпадает");
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        int subtaskId = manager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Subtask 1 updated", "Subtask 1 Description updated",
                TaskStatus.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(10));
        updatedSubtask.setId(subtaskId);
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + subtaskId))
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при обновлении подзадачи");

        Subtask storedSubtask = manager.getSubtaskById(subtaskId);
        assertNotNull(storedSubtask, "Подзадача должна существовать");
        assertEquals(updatedSubtask.getTitle(), storedSubtask.getTitle(), "Имя подзадачи не обновилось");
        assertEquals(updatedSubtask.getDescription(), storedSubtask.getDescription(), "Описание подзадачи не обновилось");
        assertEquals(TaskStatus.IN_PROGRESS, storedSubtask.getStatus(), "Статус подзадачи не обновился");
    }

    @Test
    public void shouldDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        int subtaskId = manager.createSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + subtaskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при удалении подзадачи");

        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class,
                () -> manager.getSubtaskById(subtaskId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найдена"),
                "Ожидалось исключение о том, что подзадача не найдена");
    }

    @Test
    public void shouldReturn404IfSubtaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + 999))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_FOUND.code(), response.statusCode(),
                "Некорректный статус ответа для несуществующей подзадачи");
    }

    @Test
    public void shouldReturn406IfSubtaskOverlaps() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 1, 10, 0), Duration.ofMinutes(60));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 1, 10, 30), Duration.ofMinutes(30));
        String subtaskJson = gson.toJson(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_ACCEPTABLE.code(), response.statusCode(),
                "Некорректный статус ответа для пересечения созданной подзадачи");
    }

    @Test
    public void shouldReturn406IfUpdatedSubtaskOverlaps() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 1, 10, 0), Duration.ofMinutes(60));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.NEW, epic.getId(),
                LocalDateTime.of(2025, 3, 1, 12, 0), Duration.ofMinutes(30));
        manager.createSubtask(subtask2);

        subtask2.setStartTime(LocalDateTime.of(2025, 3, 1, 10, 30)); // Конфликт по времени
        String subtaskJson = gson.toJson(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_ACCEPTABLE.code(), response.statusCode(),
                "Некорректный статус ответа для пересечения обновлённой подзадачи");
    }
}
