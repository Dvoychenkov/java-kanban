package api;

import com.google.gson.Gson;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import interfaces.TaskManager;
import managers.InMemoryTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static enums.HttpStatusCode.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerHistoryTest {
    static TaskManager manager;
    static HttpTaskServer taskServer;
    static Gson gson;
    static HttpClient client;
    static String baseTestUrl;

    @BeforeAll
    public static void beforeAll() {
        client = HttpClient.newHttpClient();

        String serverProtocol = "http://";
        int serverPort = 8080;
        String serverName = "localhost";
        String serverUrl = String.format("%s%s:%d", serverProtocol, serverName, serverPort);
        String historyPath = "/history/";
        baseTestUrl = serverUrl + historyPath;
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    public void afterEach() {
        if (taskServer == null) {
            return;
        }
        taskServer.stop();
        taskServer = null;
    }

    @Test
    public void testGetTaskHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(60));
        int taskId1 = manager.addNewTask(task1);
        int taskId2 = manager.addNewTask(task2);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(120), Duration.ofMinutes(60));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(180), Duration.ofMinutes(60));
        int subtaskId1 = manager.addNewSubtask(subtask1);
        int subtaskId2 = manager.addNewSubtask(subtask2);

        manager.getTask(taskId1);
        manager.getSubtask(subtaskId1);
        manager.getTask(taskId2);
        manager.getSubtask(subtaskId2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
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
