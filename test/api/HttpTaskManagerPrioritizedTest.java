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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerPrioritizedTest {
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
        String prioritizedPath = "/prioritized/";
        baseTestUrl = serverUrl + prioritizedPath;
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
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(120), Duration.ofMinutes(59));
        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(60), Duration.ofMinutes(59));
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(240), Duration.ofMinutes(59));
        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 Description", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(0), Duration.ofMinutes(59));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при получении приоритетов");

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Ответ не должен быть пустым");
        assertEquals(4, prioritizedTasks.length, "Некорректное количество задач");

        assertEquals(subtask2.getId(), prioritizedTasks[0].getId(),
                "Вторая подзадача должна быть первой по приоритету");
        assertEquals(task2.getId(), prioritizedTasks[1].getId(),
                "Вторая задача должна быть второй по приоритету");
        assertEquals(task1.getId(), prioritizedTasks[2].getId(),
                "Первая задача должна быть третей по приоритету");
        assertEquals(subtask1.getId(), prioritizedTasks[3].getId(),
                "Первая подзадача должна быть четвёртой по приоритету");
    }
}
