package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import interfaces.TaskManager;
import utilities.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer httpServer;
    private final TaskManager taskManager;
    private final Gson gson;

    HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        createHttpServer();
        createMapping();
    }

    HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public Gson getGson() {
        return gson;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    private void createHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    private void createMapping() {
        httpServer.createContext("/tasks", new TasksHandler(this));
        httpServer.createContext("/subtasks", new SubtasksHandler(this));
        httpServer.createContext("/epics", new EpicsHandler(this));
        httpServer.createContext("/history", new HistoryHandler(this));
        httpServer.createContext("/prioritized", new PrioritizedHandler(this));
    }

    public void start() {
        httpServer.start();
        System.out.println("Http task server started on port " + PORT);
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Http task server stopped");
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpServer = new HttpTaskServer();
        httpServer.start();
    }
}
