package utilities;

import interfaces.TaskManager;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    void getDefaultManagerShouldReturnFileBackedTaskManager() throws IOException {
        Path pathToFile = Path.of("task_manager_data.csv");
        if (Files.exists(pathToFile)) {
            Files.delete(pathToFile);
        }

        TaskManager defaultManager = Managers.getDefault();
        assertNotNull(defaultManager, "Default manager не должен быть null");
        assertInstanceOf(FileBackedTaskManager.class, defaultManager,
                "Default manager должен быть экземпляром FileBackedTaskManager");
        assertTrue(defaultManager.getTasks().isEmpty(),
                "Новый FileBackedTaskManager должен содержать пустой список задач");
    }

}
