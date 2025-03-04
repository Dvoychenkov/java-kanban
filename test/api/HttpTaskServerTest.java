package api;

import com.google.gson.Gson;
import interfaces.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.http.HttpClient;

public abstract class HttpTaskServerTest<T extends TaskManager> {

    static HttpTaskServer taskServer;
    static Gson gson;
    static HttpClient client;
    static String baseUrl;

    protected T manager;

    protected abstract T createTaskManager();

    protected HttpTaskServerTest() {
        String serverProtocol = "http://";
        int serverPort = 8080;
        String serverName = "localhost";
        baseUrl = String.format("%s%s:%d", serverProtocol, serverName, serverPort);
    }

    @BeforeAll
    public static void beforeAll() {
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        manager = createTaskManager();
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
}