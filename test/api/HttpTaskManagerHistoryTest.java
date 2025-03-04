package api;

import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import interfaces.TaskManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static enums.HttpStatusCode.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

abstract class HttpTaskManagerHistoryTest<T extends TaskManager> extends HttpTaskServerTest<T> {

    HttpTaskManagerHistoryTest() {
        super();
        baseUrl = baseUrl + "/history/";
    }

    @Test
    public void testGetTaskHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(60));
        int taskId1 = manager.createTask(task1);
        int taskId2 = manager.createTask(task2);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(120), Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(180), Duration.ofMinutes(60));
        int subtaskId1 = manager.createSubtask(subtask1);
        int subtaskId2 = manager.createSubtask(subtask2);

        manager.getTaskById(taskId1);
        manager.getSubtaskById(subtaskId1);
        manager.getTaskById(taskId2);
        manager.getSubtaskById(subtaskId2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении истории");

        Task[] historyTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(historyTasks, "История задач должна быть непустой");
        assertEquals(4, historyTasks.length, "Некорректное количество задач в истории");

        assertEquals(taskId1, historyTasks[0].getId(),
                "Первая задача в истории должна соответствовать первой просмотренной");
        assertEquals(subtaskId1, historyTasks[1].getId(),
                "Первая подзадача в истории должна соответствовать первой просмотренной");
        assertEquals(taskId2, historyTasks[2].getId(),
                "Вторая задача в истории должна соответствовать второй просмотренной");
        assertEquals(subtaskId2, historyTasks[3].getId(),
                "Первая подзадача в истории должна соответствовать первой просмотренной");
    }
}
