package utilities;

import entities.Task;
import enums.TaskStatus;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import managers.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
    }

    // Убеждаемся, что утилитарный класс всегда возвращает проинициализированный
    // и готовый к работе экземпляр менеджера задач
    @Test
    void getDefaultTaskManager() {
        assertNotNull(taskManager, "Менеджер задач не проинициализирован");
        assertNotNull(taskManager.getTasks(), "Менеджер задач содержит задачи после создания");

        int newId = taskManager.getNewId();
        assertEquals(1, newId, "Менеджер задач не вернул новый id");

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        taskManager.addNewTask(task);

        assertNotNull(taskManager.getTasks(), "Менеджер задач не добавил новую задачу");
    }

    // Убеждаемся, что утилитарный класс всегда возвращает проинициализированный
    // и готовый к работе экземпляр менеджера истории задач
    @Test
    void getDefaultHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "Менеджер истории задач не проинициализирован");
        assertNotNull(historyManager.getHistory(), "Менеджер истории задач содержит историю создания");

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);

        historyManager.add(task);
        assertNotNull(historyManager.getHistory(), "Менеджер истории задач не добавил новую задачу");
    }

    // Убеждаемся, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void addLittleMoreTasksToHistory() {
        Task task = new Task("Task title 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.addNewTask(task);
        Task savedTask = taskManager.getTask(taskId);

        List<Task> tasksHistory = taskManager.getHistory();
        int taskHistorySize = tasksHistory.size();
        Task firstTaskFromHistory = tasksHistory.getFirst();

        int firstTaskFromHistoryId = firstTaskFromHistory.getId();
        String firstTaskFromHistoryTitle = firstTaskFromHistory.getTitle();
        String firstTaskFromHistoryDescription = firstTaskFromHistory.getDescription();
        TaskStatus firstTaskFromHistoryStatus = firstTaskFromHistory.getStatus();

        savedTask.setTitle("Task title 1 updated");
        savedTask.setDescription("Task 1 description updated");
        savedTask.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTask(savedTask);
        taskManager.getTask(taskId);

        List<Task> tasksHistoryAfterUpdate = taskManager.getHistory();
        int tasksHistoryAfterUpdateSize = tasksHistoryAfterUpdate.size();
        Task firstTaskFromHistoryAfterUpdate = tasksHistoryAfterUpdate.getFirst();

        int firstTaskFromHistoryAfterUpdateId = firstTaskFromHistoryAfterUpdate.getId();
        String firstTaskFromHistoryAfterUpdateTitle = firstTaskFromHistoryAfterUpdate.getTitle();
        String firstTaskFromHistoryAfterUpdateDescription = firstTaskFromHistoryAfterUpdate.getDescription();
        TaskStatus firstTaskFromHistoryAfterUpdateStatus = firstTaskFromHistoryAfterUpdate.getStatus();

        assertEquals(taskHistorySize, tasksHistoryAfterUpdateSize,
                "Количество записей в истории должно совпадать после повторного просмотра задачи");
        assertEquals(firstTaskFromHistoryId, firstTaskFromHistoryAfterUpdateId,
                "Id первой задачи в истории изменился");
        assertNotEquals(firstTaskFromHistoryTitle, firstTaskFromHistoryAfterUpdateTitle,
                "Заголовок первой задачи в истории не изменился");
        assertNotEquals(firstTaskFromHistoryDescription, firstTaskFromHistoryAfterUpdateDescription,
                "Описание первой задачи в истории не изменилось");
        assertNotEquals(firstTaskFromHistoryStatus, firstTaskFromHistoryAfterUpdateStatus,
                "Статус первой задачи в истории не изменился");
    }

}