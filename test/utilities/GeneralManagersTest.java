package utilities;

import entities.Task;
import enums.TaskStatus;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneralManagersTest {
    TaskManager inMemoryTaskManager;
    HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        inMemoryTaskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    // Убеждаемся, что утилитарный класс всегда возвращает проинициализированный и готовый к работе экземпляр менеджера задач
    @Test
    void getDefaultTaskManager() {
        assertNotNull(inMemoryTaskManager, "Менеджер задач не проинициализирован");
        assertNotNull(inMemoryTaskManager.getTasks(), "Менеджер задач содержит задачи после создания");

        int newId = inMemoryTaskManager.getNewId();
        assertEquals(1, newId, "Менеджер задач не вернул новый id");

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        inMemoryTaskManager.addNewTask(task);

        assertNotNull(inMemoryTaskManager.getTasks(), "Менеджер задач не добавил новую задачу");
    }

    // Убеждаемся, что утилитарный класс всегда возвращает проинициализированный и готовый к работе экземпляр менеджера истории задач
    @Test
    void getDefaultHistoryManager() {
        historyManager = Managers.getDefaultHistory();

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
        int taskId = inMemoryTaskManager.addNewTask(task);
        Task savedTask = inMemoryTaskManager.getTask(taskId);

        List<Task> tasksHistory =  inMemoryTaskManager.getHistory();
        int taskHistorySize = tasksHistory.size();
        Task firstTaskFromHistory = tasksHistory.getFirst();

        int firstTaskFromHistoryId = firstTaskFromHistory.getId();
        String firstTaskFromHistoryTitle = firstTaskFromHistory.getTitle();
        String firstTaskFromHistoryDescription = firstTaskFromHistory.getDescription();
        TaskStatus firstTaskFromHistoryStatus = firstTaskFromHistory.getStatus();

        savedTask.setTitle("Task title 1 updated");
        savedTask.setDescription("Task 1 description updated");
        savedTask.setStatus(TaskStatus.IN_PROGRESS);

        inMemoryTaskManager.updateTask(savedTask);
        inMemoryTaskManager.getTask(taskId);

        List<Task> tasksHistoryAfterUpdate =  inMemoryTaskManager.getHistory();
        int tasksHistoryAfterUpdateSize = tasksHistoryAfterUpdate.size();
        Task firstTaskFromHistoryAfterUpdate = tasksHistoryAfterUpdate.getFirst();

        int firstTaskFromHistoryAfterUpdateId = firstTaskFromHistoryAfterUpdate.getId();
        String firstTaskFromHistoryAfterUpdateTitle = firstTaskFromHistoryAfterUpdate.getTitle();
        String firstTaskFromHistoryAfterUpdateDescription = firstTaskFromHistoryAfterUpdate.getDescription();
        TaskStatus firstTaskFromHistoryAfterUpdateStatus = firstTaskFromHistoryAfterUpdate.getStatus();

        assertEquals(taskHistorySize, tasksHistoryAfterUpdateSize, "Количество записей в истории должно совпадать после повторного просмотра задачи");
        assertEquals(firstTaskFromHistoryId, firstTaskFromHistoryAfterUpdateId, "Id первой задачи в истории изменился");
        assertNotEquals(firstTaskFromHistoryTitle, firstTaskFromHistoryAfterUpdateTitle, "Заголовок первой задачи в истории не изменился");
        assertNotEquals(firstTaskFromHistoryDescription, firstTaskFromHistoryAfterUpdateDescription, "Описание первой задачи в истории не изменилось");
        assertNotEquals(firstTaskFromHistoryStatus, firstTaskFromHistoryAfterUpdateStatus, "Статус первой задачи в истории не изменился");
    }

}