package api;

import com.google.gson.Gson;
import entities.Task;
import enums.TaskStatus;
import exceptions.NotFoundException;
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
import java.util.List;

import static enums.HttpStatusCode.*;
import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
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
        String tasksPath = "/tasks/";
        baseTestUrl = serverUrl + tasksPath;
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
    public void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при добавлении задачи");

        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals(task.getTitle(), tasksFromManager.getFirst().getTitle(), "Некорректный заголовок задачи");
    }

    @Test
    public void shouldGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(15));
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении задач");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Список задач не должен быть null");
        assertEquals(2, tasks.length, "Некорректное количество задач");
        assertEquals(task1.getTitle(), tasks[0].getTitle(), "Некорректный заголовок первой задачи");
        assertEquals(task2.getTitle(), tasks[1].getTitle(), "Некорректный заголовок второй задачи");
    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = manager.addNewTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl + taskId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при получении задачи");

        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(receivedTask, "Задача не должна быть null");
        assertEquals(taskId, receivedTask.getId(), "ID задачи не совпадает");
        assertEquals(task.getTitle(), receivedTask.getTitle(), "Имя задачи не совпадает");
    }

    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = manager.addNewTask(task);

        Task updatedTask = new Task("Task 1 updated", "Task 1 Description updated", TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(10));
        updatedTask.setId(taskId);
        String updatedTaskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl + taskId))
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(CREATED.code(), response.statusCode(), "Некорректный статус ответа при обновлении задачи");

        Task storedTask = manager.getTask(taskId);
        assertNotNull(storedTask, "Задача должна существовать");
        assertEquals(updatedTask.getTitle(), storedTask.getTitle(), "Имя задачи не обновилось");
        assertEquals(updatedTask.getDescription(), storedTask.getDescription(), "Описание задачи не обновилось");
        assertEquals(TaskStatus.IN_PROGRESS, storedTask.getStatus(), "Статус задачи не обновился");
    }

    @Test
    public void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = manager.addNewTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl + taskId))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(OK.code(), response.statusCode(), "Некорректный статус ответа при удалении задачи");

        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class,
                () -> manager.getTask(taskId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найдена"),
                "Ожидалось исключение о том, что задача не найдена");
    }

    @Test
    public void shouldReturn404IfTaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl + 999))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_FOUND.code(), response.statusCode(),
                "Некорректный статус ответа для несуществующей задачи");
    }

    @Test
    public void shouldReturn406IfTaskOverlaps() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 1, 10, 0), Duration.ofMinutes(60));
        manager.addNewTask(task1);

        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 1, 10, 30), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_ACCEPTABLE.code(), response.statusCode(),
                "Некорректный статус ответа для пересечения созданной задачи");
    }

    @Test
    public void shouldReturn406IfUpdatedTaskOverlaps() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Task 1 Description", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 1, 10, 0), Duration.ofMinutes(60));
        manager.addNewTask(task1);

        Task task2 = new Task("Task 2", "Task 2 Description", TaskStatus.NEW,
                LocalDateTime.of(2025, 3, 1, 12, 0), Duration.ofMinutes(30));
        manager.addNewTask(task2);

        task2.setStartTime(LocalDateTime.of(2025, 3, 1, 10, 30)); // Конфликт по времени
        String taskJson = gson.toJson(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseTestUrl))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(NOT_ACCEPTABLE.code(), response.statusCode(),
                "Некорректный статус ответа для пересечения обновлённой задачи");
    }
}
