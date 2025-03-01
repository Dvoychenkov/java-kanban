package base;

import entities.Task;
import enums.TaskStatus;
import exceptions.ManagerLoadException;
import exceptions.ManagerSaveException;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    @Override
    public FileBackedTaskManager createTaskManager() {
        File tempFile;
        try {
            tempFile = File.createTempFile("test_task_manager_data", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tempFile.deleteOnExit();
        return FileBackedTaskManager.loadFromFile(tempFile);
    }

    // Проверяем корректный перехват исключений при работе с файлами
    @Test
    void shouldHandleFileExceptionsCorrectly() {
        File invalidFile = new File("invalid/path/to/file.csv");

        ManagerLoadException managerLoadException = assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(invalidFile),
                "Ожидалось исключение при загрузке менеджера из некорректного пути");
        assertTrue(managerLoadException.getMessage().contains("Ошибка при загрузке задач"),
                "Ожидалось исключение об ошибке загрузки");

        ManagerSaveException managerSaveException = assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(invalidFile);
            manager.addNewTask(new Task("Invalid Task", "Should fail", TaskStatus.NEW));
        }, "Ожидалось исключение об ошибке записи");

    }
}
