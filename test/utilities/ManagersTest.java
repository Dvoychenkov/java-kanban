package utilities;

import interfaces.TaskManager;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    // Проверка, что дефолтный менеджер у нас файловый
    @Test
    void getDefaultManagerShouldReturnFileBackedTaskManager() throws IOException {
        // Если файл существует - удаляем и создаём новый пустой
        File storageFile = new File("task_manager_data.csv");
        Path pathToFile = storageFile.toPath();
        if (Files.exists(pathToFile)) {
            Files.delete(pathToFile);
        }
        Files.createFile(pathToFile);

        TaskManager defaultManager = Managers.getDefault();
        assertNotNull(defaultManager, "Default manager не должен быть null");
        assertInstanceOf(FileBackedTaskManager.class, defaultManager,
                "Default manager должен быть экземпляром FileBackedTaskManager");
        assertTrue(defaultManager.getTasks().isEmpty(),
                "Новый FileBackedTaskManager должен содержать пустой список задач для пустого файла");
        assertTrue(defaultManager.getSubtasks().isEmpty(),
                "Новый FileBackedTaskManager должен содержать пустой список подзадач для пустого файла");
        assertTrue(defaultManager.getEpics().isEmpty(),
                "Новый FileBackedTaskManager должен содержать пустой список эпиков для пустого файла");
    }

}
