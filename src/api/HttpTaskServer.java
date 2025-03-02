package api;

import com.sun.net.httpserver.HttpServer;
import interfaces.TaskManager;
import utilities.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer httpServer;
    private final TaskManager taskManager;

    HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        createHttpServer();
        createMapping();
    }

    HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    private void createHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    }

    private void createMapping() {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
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
