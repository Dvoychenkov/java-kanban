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

    // Проверка загрузки и сохранения из пустого файла
    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        // Загрузка из пустого файла
        assertTrue(taskManager.getTasks().isEmpty(),
                "Менеджер должен содержать пустой список задач после создания");
        assertTrue(taskManager.getSubtasks().isEmpty(),
                "Менеджер должен содержать пустой список подзадач после создания");
        assertTrue(taskManager.getEpics().isEmpty(),
                "Менеджер должен содержать пустой список эпиков после создания");

        // Наполнение задачами
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        taskManager.createTask(task1);
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(subtask1);

        assertFalse(taskManager.getTasks().isEmpty(), "Менеджер не должен содержать пустой список задач после наполнения");
        assertFalse(taskManager.getSubtasks().isEmpty(),
                "Менеджер не должен содержать пустой список подзадач после наполнения");
        assertFalse(taskManager.getEpics().isEmpty(),
                "Менеджер не должен содержать пустой список эпиков после наполнения");

        // Удаление задач и, следственно, сохранение формирование пустого файла
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getTasks().isEmpty(),
                "Менеджер должен содержать пустой список задач после удаления всех задач");
        assertTrue(taskManager.getSubtasks().isEmpty(),
                "Менеджер должен содержать пустой список подзадач после удаления всех подзадач");
        assertTrue(taskManager.getEpics().isEmpty(),
                "Менеджер должен содержать пустой список эпиков после удаления всех эпиков");

        // Загрузка из пустого файла после того, как он был изменён
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(),
                "Менеджер должен содержать пустой список задач после повторной загрузки после удаления всех задач");
        assertTrue(loadedManager.getSubtasks().isEmpty(),
                "Менеджер должен содержать пустой список подзадач после удаления всех подзадач");
        assertTrue(loadedManager.getEpics().isEmpty(),
                "Менеджер должен содержать пустой список эпиков после удаления всех эпиков");
    }

    // Проверка сохранения и загрузки нескольких задач
    @Test
    void shouldSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> savedTasks = taskManager.getTasks();
        assertEquals(2, savedTasks.size(), "Должно быть сохранено 2 задачи");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedTasks = loadedManager.getTasks();
        assertEquals(2, loadedTasks.size(), "Должно быть загружено 2 задачи");
        assertEquals(task1.getTitle(), loadedTasks.get(0).getTitle(), "Название первой задачи должно совпадать");
        assertEquals(task2.getTitle(), loadedTasks.get(1).getTitle(), "Название второй задачи должно совпадать");
    }

    // Проверка сохранения и загрузки эпика с подзадачами
    @Test
    void shouldCorrectlySaveAndLoadEpicsWithSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(subtask1);

        assertEquals(1, taskManager.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, taskManager.getSubtasks().size(), "Должна быть 1 подзадача");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getEpics().size(), "Должен загрузиться 1 эпик");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна загрузиться 1 подзадача");
    }
}
