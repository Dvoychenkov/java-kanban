package entities;

import enums.TaskStatus;

import managers.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private FileBackedTaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        File tempFile = File.createTempFile("test_task_manager_data", ".csv");
        tempFile.deleteOnExit();
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
    }

    // Тест создания задачи
    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    // Проверка, что экземпляры класса Task равны друг другу, если равен их id;
    @Test
    void tasksWithEqualIdsMustBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int task1Id = taskManager.createTask(task1);
        Task task2 = taskManager.getTaskById(task1Id);

        assertEquals(task1.getId(), task2.getId(), "Id задач не эквивалентны");
        assertEquals(task1, task2, "Задачи с одинаковыми id должны быть эквивалентны");
    }

    // Проверка, что экземпляры класса Task не равны друг другу, если не равен их id;
    @Test
    void tasksWithDifferentIdsMustNotBeEqual() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Id задач эквивалентны");
        assertNotEquals(task1, task2, "Задачи с разными id не должны быть эквивалентны");
    }

    // Проверка, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    void tasksWithGeneratedIdAndSetIdMustNotConflict() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int task1Id = taskManager.createTask(task1);
        Task savedTask1 = taskManager.getTaskById(task1Id);

        Task task2 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        task2.setId(task1Id);
        int task2Id = taskManager.createTask(task2);
        Task savedTask2 = taskManager.getTaskById(task2Id);

        assertNotEquals(savedTask1.getId(), savedTask2.getId(), "Id задач эквивалентны");
        assertNotEquals(savedTask1, savedTask2, "Задачи эквивалентны");
    }

    // Проверка неизменности задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void checkTaskMustNotChangedAfterAddingIntoManager() {
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);

        Task receivedTask = taskManager.getTaskById(taskId);
        int receivedTaskId = receivedTask.getId();
        String receivedTaskTitle = receivedTask.getTitle();
        String receivedTaskDescription = receivedTask.getDescription();
        TaskStatus receivedTaskStatus = receivedTask.getStatus();

        task.setId(999);
        task.setTitle("Title 1 try to change not in manager");
        task.setDescription("Description 1 try to change not in manager");
        task.setStatus(TaskStatus.IN_PROGRESS);

        Task receivedTaskAgain = taskManager.getTaskById(taskId);
        int receivedTaskAgainId = receivedTaskAgain.getId();
        String receivedTaskAgainTitle = receivedTaskAgain.getTitle();
        String receivedTaskAgainDescription = receivedTaskAgain.getDescription();
        TaskStatus receivedTaskAgainStatus = receivedTaskAgain.getStatus();

        assertEquals(receivedTaskId, receivedTaskAgainId, "Задача была изменена не в менеджере, изменился id");
        assertEquals(receivedTaskTitle, receivedTaskAgainTitle, "Задача была изменена не в менеджере, изменился заголовок");
        assertEquals(receivedTaskDescription, receivedTaskAgainDescription, "Задача была изменена не в менеджере, изменилось описание");
        assertEquals(receivedTaskStatus, receivedTaskAgainStatus, "Задача была изменена не в менеджере, изменился статус");
    }

    // Проверка того, что задача после добавления в менеджере не меняется внутри него после изменения значения у изначального объекта
    @Test
    void checkTaskNotChangedInnerAfterUpdateExternal() {
        Task task = new Task("Task new", "Task new description", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        Task savedTask = taskManager.getTaskById(taskId);

        savedTask.setTitle("New title");
        taskManager.updateTask(savedTask);
        Task updatedTask = taskManager.getTaskById(taskId);

        String taskTitle = updatedTask.getTitle();

        savedTask.setTitle("Newest title");
        Task updatedTaskAgain = taskManager.getTaskById(taskId);
        String taskTitleAgain = updatedTaskAgain.getTitle();

        assertEquals(taskTitle, taskTitleAgain, "Заголовок задачи не должен был измениться");
    }

}
