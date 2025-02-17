package utilities;

import entities.Task;
import enums.TaskStatus;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    // Тест пустой истории
    @Test
    void getHistory() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(0, history.size(), "История не пустая.");
    }

    // Тест добавления задачи в историю напрямую
    @Test
    void addTaskToHistory() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
    }

    // Тест добавления и удаления одной и той же задачи в историю напрямую
    @Test
    void addAndRemoveSameTaskToHistory() {
        List<Task> history = historyManager.getHistory();

        int historySize = history.size();
        int expectedHistorySize = 0;
        String expectedHistorySizeMsg = "Размер истории равен %d, ожидалось %d.";
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        task.setId(1);

        historyManager.add(task);
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 1;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.add(task);
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.add(task);
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 0;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertFalse(historySize < 0, "Размер истории содержит отрицательное значение");
    }

    // Тест добавления и удаления разных задач в историю напрямую
    @Test
    void addAndRemoveDifferentTasksToHistory() {
        List<Task> history = historyManager.getHistory();

        int historySize = history.size();
        int expectedHistorySize = 0;
        String expectedHistorySizeMsg = "Размер истории равен %d, ожидалось %d.";
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        Task task1 = new Task("Test addNewTask 1", "Test addNewTask description 1", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Test addNewTask 2", "Test addNewTask description 2", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Test addNewTask 3", "Test addNewTask description 3", TaskStatus.NEW);
        task3.setId(3);
        Task task4 = new Task("Test addNewTask 4", "Test addNewTask description 4", TaskStatus.NEW);
        task4.setId(4);

        historyManager.add(task1);
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 1;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.add(task1);
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));


        historyManager.add(task2);
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 2;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.add(task2);
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));


        historyManager.add(task3);
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 3;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.add(task3);
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));


        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 2;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task2.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 1;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task2.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));


        historyManager.remove(task3.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        expectedHistorySize = 0;
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));

        historyManager.remove(task3.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));


        historyManager.remove(task4.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));
        assertFalse(historySize < 0, "Размер истории содержит отрицательное значение");

        historyManager.remove(task4.getId());
        history = historyManager.getHistory();
        historySize = history.size();
        assertEquals(expectedHistorySize, historySize, String.format(expectedHistorySizeMsg, historySize, expectedHistorySize));
        assertFalse(historySize < 0, "Размер истории содержит отрицательное значение");
    }
}
