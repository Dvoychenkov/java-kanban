package utilities;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test_task_manager_data", ".csv");
        tempFile.deleteOnExit();
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
    }

    // Проверка, что задачи не загружены для нового инстанса
    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(Files.readString(tempFile.toPath()).isBlank(), "Файл должен оставаться пустым");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        List<Task> savedTasks = taskManager.getTasks();
        assertEquals(2, savedTasks.size(), "Должно быть сохранено 2 задачи");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedTasks = loadedManager.getTasks();
        assertEquals(2, loadedTasks.size(), "Должно быть загружено 2 задачи");
        assertEquals(task1.getTitle(), loadedTasks.get(0).getTitle(), "Название первой задачи должно совпадать");
        assertEquals(task2.getTitle(), loadedTasks.get(1).getTitle(), "Название второй задачи должно совпадать");
    }

    @Test
    void shouldCorrectlySaveAndLoadEpicsWithSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description", TaskStatus.DONE, epic.getId());
        taskManager.addNewSubtask(subtask1);

        assertEquals(1, taskManager.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, taskManager.getSubtasks().size(), "Должна быть 1 подзадача");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна загрузиться 1 подзадача");
    }
}
