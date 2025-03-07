package base;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import exceptions.NotFoundException;
import exceptions.TaskIntersectionException;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Проверка всех реализованных методов интерфейса TaskManager + дополнительные кейсы
abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    @Test
    void shouldCreateTask() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
    }

    @Test
    void shouldAddNewEpic() {
        Epic epic = new Epic("Epic", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpicById(epicId);
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
    }

    @Test
    void shouldAddNewSubtask() {
        Epic epic = new Epic("Epic", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Subtask description", TaskStatus.NEW, epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
    }

    @Test
    void shouldDeleteTaskById() {
        Task task = new Task("Task", "Description", TaskStatus.NEW);
        int taskId = taskManager.createTask(task);
        taskManager.deleteTask(taskId);
        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class, () -> taskManager.getTaskById(taskId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найдена"),
                "Ожидалось исключение о том, что задача не найдена");
    }

    @Test
    void shouldDeleteSubtaskById() {
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW);
        int subtaskId = taskManager.createTask(subtask);
        taskManager.deleteSubtask(subtaskId);
        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class, () -> taskManager.getSubtaskById(subtaskId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найдена"),
                "Ожидалось исключение о том, что подзадача не найдена");
    }

    @Test
    void shouldDeleteEpicById() {
        Epic epic = new Epic("Epic", "Description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        taskManager.deleteEpic(epicId);
        NotFoundException notFoundExceptionotFoundException = assertThrows(NotFoundException.class, () -> taskManager.getEpicById(epicId));
        assertTrue(notFoundExceptionotFoundException.getMessage().contains("не найден"),
                "Ожидалось исключение о том, что эпик не найден");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask(new Task("Task 1", "Description", TaskStatus.NEW));
        taskManager.createTask(new Task("Task 2", "Description", TaskStatus.NEW));
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Все задачи должны быть удалены");
    }

    @Test
    void shouldDeleteAllSubtasks() {
        Epic epic = new Epic("Epic", "Description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        taskManager.createSubtask(new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId));
        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Все подзадачи должны быть удалены");
    }

    @Test
    void shouldDeleteAllEpics() {
        taskManager.createEpic(new Epic("Epic 1", "Description", TaskStatus.NEW));
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Все эпики должны быть удалены");
    }

    // Проверка, что нельзя указать подзадачу в виде самой себя в качестве эпика
    @Test
    void preventSelfReferencingEpic() {
        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW);
        int subtaskId = taskManager.createSubtask(subtask);
        subtask.setEpicId(subtaskId);

        taskManager.updateTask(subtask);
        subtask = taskManager.getSubtaskById(subtaskId);
        assertNotEquals(subtask.getEpicId(), subtask.getId(), "Подзадача не должна ссылаться на саму себя");
    }

    @Test
    void getHistoryShouldReturnsCorrectOrder() {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        int task1Id = taskManager.createTask(task1);
        int task2Id = taskManager.createTask(task2);
        taskManager.getTaskById(task1Id);
        taskManager.getTaskById(task2Id);
        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История содержит неправильное количество задач.");
        assertEquals(task1Id, history.get(0).getId(), "Порядок истории неправильный.");
        assertEquals(task2Id, history.get(1).getId(), "Порядок истории неправильный.");
    }

    /*
        Проверка расчёта статуса для эпика, а также наличия корректно связанного эпика у подзадач:

        Для расчёта статуса Epic. Граничные условия:
        a. Все подзадачи со статусом NEW.
        b. Все подзадачи со статусом DONE.
        c. Подзадачи со статусами NEW и DONE.
        d. Подзадачи со статусом IN_PROGRESS.

        Для подзадач необходимо дополнительно убедиться в наличии связанного эпика.
        Для эпиков нужно проверить корректность расчёта статуса на основании состояния подзадач.
     */
    @Test
    void shouldCalculateEpicStatusCorrectly() {
        Epic epic = new Epic("Epic", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(epicId);

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epicId).getStatus(),
                "Статус нового эпика должен быть NEW");

        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);
        int subtask1Id = taskManager.createSubtask(subtask1);
        subtask1 = taskManager.getSubtaskById(subtask1Id);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId);
        int subtask2Id = taskManager.createSubtask(subtask2);
        subtask2 = taskManager.getSubtaskById(subtask2Id);

        assertEquals(TaskStatus.NEW, taskManager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть NEW, если все подзадачи NEW");

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        subtask1 = taskManager.getSubtaskById(subtask1Id);

        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        subtask2 = taskManager.getSubtaskById(subtask2Id);

        assertEquals(TaskStatus.DONE, taskManager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть DONE, если все подзадачи DONE");

        subtask1.setStatus(TaskStatus.NEW);
        taskManager.updateSubtask(subtask1);
        subtask1 = taskManager.getSubtaskById(subtask1Id);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть IN_PROGRESS, если подзадачи NEW и DONE");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        subtask1 = taskManager.getSubtaskById(subtask1Id);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        subtask2 = taskManager.getSubtaskById(subtask2Id);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpicById(epicId).getStatus(),
                "Статус эпика должен быть IN_PROGRESS, если все подзадачи IN_PROGRESS");

        assertEquals(taskManager.getEpicById(subtask1.getEpicId()), taskManager.getEpicById(epic.getId()),
                "Подзадача 1 не содержит корректный связанный эпик");
        assertEquals(taskManager.getEpicById(subtask2.getEpicId()), taskManager.getEpicById(epic.getId()),
                "Подзадача 2 не содержит корректный связанный эпик");

        Subtask[] subtasksAddedToEpic = new Subtask[]{subtask1, subtask2};
        Subtask[] epicSubtasksByEpicId = taskManager.getEpicSubtasks(taskManager.getEpicById(epic.getId()))
                .toArray(new Subtask[0]);
        Subtask[] epicSubtasksByEpic = taskManager.getEpicSubtasks(taskManager.getEpicById(epic.getId()))
                .toArray(new Subtask[0]);
        assertArrayEquals(subtasksAddedToEpic, epicSubtasksByEpicId,
                "Эпик содержит некорректный набор подзадач");
        assertArrayEquals(subtasksAddedToEpic, epicSubtasksByEpic,
                "Эпик содержит некорректный набор подзадач");

        taskManager.deleteEpicSubtasks(taskManager.getEpicById(epic.getId()));
        assertEquals(0, taskManager.getEpicSubtasks(taskManager.getEpicById(epic.getId())).size(),
                "Эпик содержит подзадачи после очистки");
    }


    /*
        Проверка истории после всех вариантов манипуляций с менеджером и сущностями.

        Для HistoryManager — тесты для всех методов интерфейса. Граничные условия:
        a. Пустая история задач.
        b. Дублирование.
        c. Удаление из истории: начало, середина, конец.
     */
    @Test
    void shouldManageHistoryCorrectly() {
        assertEquals(0, taskManager.getHistory().size(),
                "История не должна содержать записей после создания инстанса");

        Task taskEntity1 = new Task("Task entity 1", "Description entity 1", TaskStatus.NEW);
        Task taskEntity2 = new Task("Task entity 2", "Description entity 2", TaskStatus.NEW);
        Epic epicEntity3 = new Epic("Epic entity 3", "Description entity 3", TaskStatus.NEW);
        Subtask subtaskEntity4 = new Subtask("Subtask entity 4", "Description entity 4", TaskStatus.NEW);
        Subtask subtaskEntity5 = new Subtask("Subtask entity 5", "Description entity 5", TaskStatus.NEW);

        int taskEntity1Id = taskManager.createTask(taskEntity1);
        int taskEntity2Id = taskManager.createTask(taskEntity2);
        int epicEntity3Id = taskManager.createEpic(epicEntity3);
        int subtaskEntity4Id = taskManager.createSubtask(subtaskEntity4);
        int subtaskEntity5Id = taskManager.createSubtask(subtaskEntity5);

        assertEquals(0, taskManager.getHistory().size(),
                "История не должна содержать записей после добавления сущностей");

        taskManager.getTaskById(taskEntity1Id);
        assertEquals(1, taskManager.getHistory().size(),
                "История должна содержать 1 запись после просмотра");
        taskEntity1 = taskManager.getTaskById(taskEntity1Id);
        assertEquals(1, taskManager.getHistory().size(),
                "История должна содержать 1 запись после повторного просмотра той же задачи");

        taskEntity1.setDescription(taskEntity1.getDescription() + " updated");
        taskManager.updateTask(taskEntity1);
        assertEquals(1, taskManager.getHistory().size(),
                "История должна содержать 1 запись после обновления задачи");

        taskManager.getEpicById(epicEntity3Id);
        assertEquals(2, taskManager.getHistory().size(),
                "История должна содержать 2 записи после просмотра эпика");
        epicEntity3 = taskManager.getEpicById(epicEntity3Id);
        assertEquals(2, taskManager.getHistory().size(),
                "История должна содержать 2 записи после повторного просмотра того же эпика");

        epicEntity3.setDescription(epicEntity3.getDescription() + " updated");
        taskManager.updateEpic(epicEntity3);
        assertEquals(2, taskManager.getHistory().size(),
                "История должна содержать 2 записи после обновления эпика");

        taskManager.getSubtaskById(subtaskEntity5Id);
        assertEquals(3, taskManager.getHistory().size(),
                "История должна содержать 3 записи после просмотра подзадачи");
        subtaskEntity5 = taskManager.getSubtaskById(subtaskEntity5Id);
        assertEquals(3, taskManager.getHistory().size(),
                "История должна содержать 3 записи после повторного просмотра той же подзадачи");

        subtaskEntity5.setDescription(subtaskEntity5.getDescription() + " updated");
        taskManager.updateSubtask(subtaskEntity5);
        assertEquals(3, taskManager.getHistory().size(),
                "История должна содержать 3 записи после обновления задачи");


        // Перезапускаем для упрощения определения корректности удаления из разных мест истории
        setUp();

        taskEntity1Id = taskManager.createTask(taskEntity1);
        taskEntity2Id = taskManager.createTask(taskEntity2);
        epicEntity3Id = taskManager.createEpic(epicEntity3);
        subtaskEntity4Id = taskManager.createSubtask(subtaskEntity4);
        subtaskEntity5Id = taskManager.createSubtask(subtaskEntity5);

        taskManager.getTaskById(taskEntity1Id);
        taskManager.getTaskById(taskEntity2Id);
        taskManager.getEpicById(epicEntity3Id);
        taskManager.getSubtaskById(subtaskEntity4Id);
        taskManager.getSubtaskById(subtaskEntity5Id);

        assertEquals(5, taskManager.getHistory().size(),
                "История должна содержать 5 записей после просмотра 5-ти сущностей");

        Task[] entitiesArrAfterRemoveFirst = new Task[]{
                taskManager.getHistory().get(1),
                taskManager.getHistory().get(2),
                taskManager.getHistory().get(3),
                taskManager.getHistory().get(4)
        };
        Task[] entitiesArrAfterRemoveMiddle = new Task[]{
                taskManager.getHistory().get(1),
                taskManager.getHistory().get(3),
                taskManager.getHistory().get(4)
        };
        Task[] entitiesArrAfterRemoveLast = new Task[]{
                taskManager.getHistory().get(1),
                taskManager.getHistory().get(3)
        };

        // Удаляем из начала
        taskManager.deleteTask(taskEntity1Id);
        assertEquals(4, taskManager.getHistory().size(),
                "История должна содержать 2 записи после удаления первой сущности");
        assertArrayEquals(entitiesArrAfterRemoveFirst, taskManager.getHistory().toArray(),
                "Порядок истории некорректен после удаления последней сущности");

        // Удаляем из середины
        taskManager.deleteEpic(epicEntity3Id);
        assertEquals(3, taskManager.getHistory().size(),
                "История должна содержать 3 записи после удаления третьей сущности");
        assertArrayEquals(entitiesArrAfterRemoveMiddle, taskManager.getHistory().toArray(),
                "Порядок истории некорректен после удаления последней сущности");

        // Удаляем из конца
        taskManager.deleteSubtask(subtaskEntity5Id);
        assertEquals(2, taskManager.getHistory().size(),
                "История должна содержать 4 записи после удаления последней сущности");
        assertArrayEquals(entitiesArrAfterRemoveLast, taskManager.getHistory().toArray(),
                "Порядок истории некорректен после удаления последней сущности");

        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();
        assertEquals(0, taskManager.getHistory().size(),
                "История не должна содержать записей после удаления всех записей");
    }

    // Убеждаемся, что реализован корректный расчёт пересечения временных интервалов задач, чтобы предотвратить конфликтные ситуации.
    @Test
    void shouldDetectTaskTimeIntersection() {
        Task task1 = new Task("Task entity 1", "Description", TaskStatus.NEW);
        task1.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 10, 0));
        task1.setDuration(java.time.Duration.ofMinutes(60));
        taskManager.createTask(task1);

        Task task2 = new Task("Task entity 2", "Description", TaskStatus.NEW);
        task2.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 10, 30));
        task2.setDuration(java.time.Duration.ofMinutes(60));
        TaskIntersectionException taskIntersectionException = assertThrows(TaskIntersectionException.class, () -> taskManager.createTask(task2));
        assertTrue(taskIntersectionException.getMessage().contains("пересекается по времени выполнения"),
                "Ожидалось исключение о пересечении времени");

        Task task3 = new Task("Task entity 3", "Description", TaskStatus.NEW);
        task3.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 11, 0));
        task3.setDuration(java.time.Duration.ofMinutes(60));
        assertDoesNotThrow(() -> taskManager.createTask(task3), "Не должно выбрасываться исключений");

        Subtask subtask4 = new Subtask("Subtask entity 4", "Description", TaskStatus.NEW);
        subtask4.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 12, 0));
        subtask4.setDuration(java.time.Duration.ofMinutes(60));
        assertDoesNotThrow(() -> taskManager.createSubtask(subtask4), "Не должно выбрасываться исключений");

        Subtask subtask5 = new Subtask("Subtask entity 5", "Description", TaskStatus.NEW);
        subtask5.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 9, 0));
        subtask5.setDuration(java.time.Duration.ofMinutes(60));
        assertDoesNotThrow(() -> taskManager.createSubtask(subtask5), "Не должно выбрасываться исключений");

        Task task6 = new Task("Task entity 6", "Description", TaskStatus.NEW);
        task6.setStartTime(java.time.LocalDateTime.of(2025, 2, 28, 9, 0));
        task6.setDuration(java.time.Duration.ofMinutes(60));
        taskIntersectionException = assertThrows(TaskIntersectionException.class, () -> taskManager.createTask(task6));
        assertTrue(taskIntersectionException.getMessage().contains("пересекается по времени выполнения"),
                "Ожидалось исключение о пересечении времени");
    }

    // Проверка подсчёта временных полей эпика без подзадач
    @Test
    void shouldCalculateEpicTimeWithNoSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpicById(epicId);

        assertEquals(Duration.ZERO, savedEpic.getDuration(), "Длительность эпика без подзадач должна быть равна нулю");
        assertNull(savedEpic.getStartTime(), "Время начала эпика без подзадач должно быть null");
        assertNull(savedEpic.getEndTime(), "Время окончания эпика без подзадач должно быть null");
    }

    // Проверка подсчёта временных полей эпика с одной подзадачей
    @Test
    void shouldCalculateEpicTimeWithOneSubtask() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, epicId);
        subtask.setStartTime(LocalDateTime.of(2025, 2, 28, 10, 0));
        subtask.setDuration(Duration.ofMinutes(60));
        taskManager.createSubtask(subtask);

        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(subtask.getDuration(), updatedEpic.getDuration(),
                "Длительность эпика должна совпадать с длительностью подзадачи");
        assertEquals(subtask.getStartTime(), updatedEpic.getStartTime(),
                "Время начала эпика должно совпадать со временем начала подзадачи");
        assertEquals(subtask.getEndTime(), updatedEpic.getEndTime(),
                "Время окончания эпика должно совпадать со временем окончания подзадачи");
    }

    // Проверка подсчёта временных полей эпика с несколькими подзадачами
    @Test
    void shouldCalculateEpicTimeWithMultipleSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.of(2025, 2, 28, 10, 0));

        subtask1.setDuration(Duration.ofMinutes(60));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask description", TaskStatus.NEW, epicId);
        subtask2.setStartTime(LocalDateTime.of(2025, 2, 28, 12, 0));
        subtask2.setDuration(Duration.ofMinutes(90));
        taskManager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask 3", "Subtask description", TaskStatus.NEW, epicId);
        subtask3.setStartTime(LocalDateTime.of(2025, 2, 28, 14, 0));
        subtask3.setDuration(Duration.ofMinutes(30));
        taskManager.createSubtask(subtask3);

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(Duration.ofMinutes(60 + 90 + 30), updatedEpic.getDuration(), "Длительность эпика должна быть суммой всех подзадач");
        assertEquals(subtask1.getStartTime(), updatedEpic.getStartTime(), "Время начала эпика должно совпадать с самой ранней подзадачей");
        assertEquals(subtask3.getEndTime(), updatedEpic.getEndTime(), "Время окончания эпика должно совпадать с самой поздней подзадачей");
    }

    // Проверка актуализации таймингов эпика после удаления его подзадач
    @Test
    void shouldResetEpicTimeAfterRemovingAllSubtasks() {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask description", TaskStatus.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.of(2025, 2, 28, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(60));
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask description", TaskStatus.NEW, epicId);
        subtask2.setStartTime(LocalDateTime.of(2025, 2, 28, 12, 0));
        subtask2.setDuration(Duration.ofMinutes(90));
        int subtaskId2 = taskManager.createSubtask(subtask2);

        // Удаляем все подзадачи, которые привязаны к эпику
        taskManager.deleteSubtask(subtaskId1);
        taskManager.deleteSubtask(subtaskId2);

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertEquals(Duration.ZERO, updatedEpic.getDuration(), "После удаления всех подзадач длительность эпика должна быть 0");
        assertNull(updatedEpic.getStartTime(), "После удаления всех подзадач время начала эпика должно быть null");
        assertNull(updatedEpic.getEndTime(), "После удаления всех подзадач время окончания эпика должно быть null");
    }
}