package entities;

import enums.TaskStatus;
import interfaces.TaskManager;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        File tempFile = File.createTempFile("test_task_manager_data", ".csv");
        tempFile.deleteOnExit();
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
    }

    // Тест создания подзадачи
    @Test
    void addNewSubtask() {
        Subtask subtask = new Subtask("Subtask addNewSubtask", "Subtask addNewSubtask description", TaskStatus.NEW);
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    // Тест создания подзадачи, затем эпика для неё плюс проверка их статусов
    @Test
    void addNewSubtaskAndEpicAndLinkThemAndCheckStatuses() {
        Subtask subtask = new Subtask("Subtask addNewSubtask", "Subtask addNewSubtask description", TaskStatus.IN_PROGRESS);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        List<Integer> epicsSubtasksIds = new ArrayList<>();
        epicsSubtasksIds.add(subtaskId);

        Epic epic = new Epic("Epic addNewEpic", "Epic addNewEpic description", TaskStatus.NEW);
        epic.setSubtasksIds(epicsSubtasksIds);
        int epicId = taskManager.createEpic(epic);

        savedSubtask.setEpicId(epicId);
        taskManager.updateSubtask(savedSubtask);
        Subtask updatedSubtask = taskManager.getSubtaskById(subtaskId);

        Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertNotNull(updatedSubtask, "Подзадача не найдена.");

        assertEquals(updatedSubtask.getEpicId(), savedEpic.getId(), "Эпик не указан для подзадачи");

        List<Integer> savedEpicsSubtasksIds = savedEpic.getSubtasksIds();

        assertNotNull(savedEpicsSubtasksIds, "Список подзадач эпика не проинициализирован");
        assertNotEquals(true, savedEpicsSubtasksIds.isEmpty(), "Список подзадач эпика пустой");

        assertEquals(updatedSubtask.getId(), savedEpicsSubtasksIds.getFirst(), "Подзадача не находится в списке подзадач эпика");

        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus(), "Статус подзадачи некорректный");
        assertEquals(TaskStatus.IN_PROGRESS, savedEpic.getStatus(), "Статус эпика некорректный");
    }

    // Проверка, что объект Subtask нельзя сделать своим же эпиком;
    @Test
    void subtaskMustNotBeSetAsYourselfEpic() {
        Subtask subtask = new Subtask("Subtask addNewSubtask", "Subtask addNewSubtask description", TaskStatus.NEW);
        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(0, subtask.getEpicId(), "Подзадача привязана к эпику сразу после создания");

        subtask.setEpicId(subtaskId);
        taskManager.updateSubtask(subtask);

        assertEquals(0, subtask.getEpicId(), "Подзадача привязана к самой себе в виде эпика");
    }

    // Проверка неизменности подзадачи (по всем полям) при добавлении подзадачи в менеджер
    @Test
    void checkSubtaskMustNotChangedAfterAddingIntoManager() {
        Subtask subtask = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, 42);
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask receivedSubtask = taskManager.getSubtaskById(subtaskId);
        int receivedSubtaskId = receivedSubtask.getId();
        String receivedSubtaskTitle = receivedSubtask.getTitle();
        String receivedSubtaskDescription = receivedSubtask.getDescription();
        TaskStatus receivedSubtaskStatus = receivedSubtask.getStatus();
        int receivedSubtaskEpicId = receivedSubtask.getEpicId();

        subtask.setId(999);
        subtask.setTitle("Title 1 try to change not in manager");
        subtask.setDescription("Description 1 try to change not in manager");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setEpicId(100500);

        Subtask receivedSubtaskAgain = taskManager.getSubtaskById(subtaskId);
        int receivedSubtaskAgainId = receivedSubtaskAgain.getId();
        String receivedSubtaskAgainTitle = receivedSubtaskAgain.getTitle();
        String receivedSubtaskAgainDescription = receivedSubtaskAgain.getDescription();
        TaskStatus receivedSubtaskAgainStatus = receivedSubtaskAgain.getStatus();
        int receivedSubtaskAgainEpicId = receivedSubtaskAgain.getEpicId();

        assertEquals(receivedSubtaskId, receivedSubtaskAgainId, "Подзадача была изменена не в менеджере, изменился id");
        assertEquals(receivedSubtaskTitle, receivedSubtaskAgainTitle, "Подзадача была изменена не в менеджере, изменился заголовок");
        assertEquals(receivedSubtaskDescription, receivedSubtaskAgainDescription, "Подзадача была изменена не в менеджере, изменилось описание");
        assertEquals(receivedSubtaskStatus, receivedSubtaskAgainStatus, "Подзадача была изменена не в менеджере, изменился статус");
        assertEquals(receivedSubtaskEpicId, receivedSubtaskAgainEpicId, "Подзадача была изменена не в менеджере, изменился id эпика");
    }

    // Проверка того, что задача после добавления в менеджере не меняется внутри него после изменения значения у изначального объекта
    @Test
    void checkSubtaskNotChangedInnerAfterUpdateExternal() {
        Subtask subtask = new Subtask("Subtask new", "Subtask new description", TaskStatus.NEW);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);

        savedSubtask.setTitle("New title");
        taskManager.updateSubtask(savedSubtask);
        Subtask updatedSubtask = taskManager.getSubtaskById(subtaskId);

        String subtaskTitle = updatedSubtask.getTitle();

        savedSubtask.setTitle("Newest title");
        Task updatedSubtaskAgain = taskManager.getSubtaskById(subtaskId);
        String subtaskTitleAgain = updatedSubtaskAgain.getTitle();

        assertEquals(subtaskTitle, subtaskTitleAgain, "Заголовок подзадачи не должен был измениться");
    }

}