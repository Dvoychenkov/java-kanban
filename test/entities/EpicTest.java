package entities;

import enums.TaskStatus;

import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.Managers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    static TaskManager taskManager;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
    }

    // Тест создания эпика
    @Test
    void addNewEpic() {
        Epic epic = new Epic("Epic addNewEpic", "Epic addNewEpic description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    // Тест создания эпика, затем подзадачи для него плюс проверка их статусов
    @Test
    void addNewEpicAndSubtaskAndLinkThemAndCheckStatuses() {
        Epic epic = new Epic("Epic addNewEpic", "Epic addNewEpic description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);
        Epic savedEpic = taskManager.getEpic(epicId);

        Subtask subtask = new Subtask("Subtask addNewSubtask", "Subtask addNewSubtask description", TaskStatus.IN_PROGRESS);
        int subtaskId = taskManager.addNewSubtask(subtask);
        Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        savedSubtask.setEpicId(epicId);

        List<Integer> epicsSubtasksIds = new ArrayList<>();
        epicsSubtasksIds.add(subtaskId);
        savedEpic.setSubtasksIds(epicsSubtasksIds);
        taskManager.updateEpic(savedEpic);
        Epic updatedEpic = taskManager.getEpic(epicId);

        assertNotNull(updatedEpic, "Эпик не найден.");
        assertNotNull(savedSubtask, "Подзадача не найдена.");

        assertEquals(savedSubtask.getEpicId(), updatedEpic.getId(), "Эпик не указан для подзадачи");

        List<Integer> updatedEpicsSubtasksIds = updatedEpic.getSubtasksIds();

        assertNotNull(updatedEpicsSubtasksIds, "Список подзадач эпика не проинициализирован");
        assertNotEquals(true, updatedEpicsSubtasksIds.isEmpty(), "Список подзадач эпика пустой");
        assertEquals(savedSubtask.getId(), updatedEpicsSubtasksIds.getFirst(), "Подзадача не находится в списке подзадач эпика");
    }

    // Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    void epicMustNotBeAddedIntoYourselfAsEpic() {
        Epic epic = new Epic("Epic addNewEpic", "Epic addNewEpic description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);

        assertNotNull(epic.getSubtasksIds(), "Список подзадач эпика не проинициализирован");
        assertTrue(epic.getSubtasksIds().isEmpty(), "Эпик содержит подзадачи сразу после создания");

        List<Integer> epicSubtasksIds = new ArrayList<>();
        epicSubtasksIds.add(epicId);
        epic.setSubtasksIds(epicSubtasksIds);
        taskManager.updateEpic(epic);

        assertNotNull(epic.getSubtasksIds(), "Список подзадач эпика не проинициализирован");
        assertTrue(epic.getSubtasksIds().isEmpty(), "Эпик добавлен в самого себя в виде подзадачи");
    }

    // Проверка неизменности эпика (по всем полям) при добавлении эпика в менеджер
    @Test
    void checkEpicMustNotChangedAfterAddingIntoManager() {
        Epic epic = new Epic("Subtask 1", "Description 1", TaskStatus.NEW, new int[] {42});
        int epicId = taskManager.addNewEpic(epic);

        Epic receivedEpic = taskManager.getEpic(epicId);
        int receivedEpicId = receivedEpic.getId();
        String receivedEpicTitle = receivedEpic.getTitle();
        String receivedEpicDescription = receivedEpic.getDescription();
        TaskStatus receivedEpicStatus = receivedEpic.getStatus();
        List<Integer> receivedEpicSubtasksIds = receivedEpic.getSubtasksIds();
        int receivedEpicSubtaskId = -1;
        if (!receivedEpicSubtasksIds.isEmpty()) {
            receivedEpicSubtaskId = receivedEpicSubtasksIds.getFirst();
        }

        epic.setId(999);
        epic.setTitle("Title 1 try to change not in manager");
        epic.setDescription("Description 1 try to change not in manager");
        epic.setStatus(TaskStatus.IN_PROGRESS);
        List<Integer> newEpicSubtasks = new ArrayList<>();
        newEpicSubtasks.add(100500);
        epic.setSubtasksIds(newEpicSubtasks);

        Epic receivedEpicAgain = taskManager.getEpic(epicId);
        int receivedEpicAgainId = receivedEpicAgain.getId();
        String receivedEpicAgainTitle = receivedEpicAgain.getTitle();
        String receivedEpicAgainDescription = receivedEpicAgain.getDescription();
        TaskStatus receivedEpicAgainStatus = receivedEpicAgain.getStatus();
        List<Integer> receivedEpicAgainSubtasksIds = receivedEpic.getSubtasksIds();

        int receivedEpicAgainSubtaskId = -1;
        if (!receivedEpicSubtasksIds.isEmpty()) {
            receivedEpicAgainSubtaskId = receivedEpicAgainSubtasksIds.getFirst();
        }

        assertEquals(receivedEpicId, receivedEpicAgainId, "Эпик был изменен не в менеджере, изменился id");
        assertEquals(receivedEpicTitle, receivedEpicAgainTitle, "Эпик был изменен не в менеджере, изменился заголовок");
        assertEquals(receivedEpicDescription, receivedEpicAgainDescription, "Эпик был изменен не в менеджере, изменилось описание");
        assertEquals(receivedEpicStatus, receivedEpicAgainStatus, "Эпик был изменен не в менеджере, изменился статус");
        assertEquals(receivedEpicSubtaskId, receivedEpicAgainSubtaskId, "Эпик был изменен не в менеджере, изменился id подзадачи");
    }

    // Проверка того, что эпик после добавления в менеджере не меняется внутри него после изменения значения у изначального объекта
    @Test
    void checkEpicNotChangedInnerAfterUpdateExternal() {
        Epic epic = new Epic("Epic new", "Epic new description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);
        Epic savedEpic = taskManager.getEpic(epicId);

        savedEpic.setTitle("New title");
        taskManager.updateEpic(savedEpic);
        Epic updatedEpic = taskManager.getEpic(epicId);

        String epicTitle = updatedEpic.getTitle();

        savedEpic.setTitle("Newest title");
        Epic updatedEpicAgain = taskManager.getEpic(epicId);
        String epicTitleAgain = updatedEpicAgain.getTitle();

        assertEquals(epicTitle, epicTitleAgain, "Заголовок эпика не должен был измениться");
    }

    // Внутри эпиков не должно оставаться неактуальных id подзадач
    @Test
    void checkEpicSubtasksInActualState() {
        // Создаём эпик без подзадач, добавляем в менеджер
        Epic epic = new Epic("Epic addNewEpic", "Epic addNewEpic description", TaskStatus.NEW);
        int epicId = taskManager.addNewEpic(epic);
        Epic savedEpic = taskManager.getEpic(epicId);

        List<Integer> epicSubtasksIds;
        epicSubtasksIds = new ArrayList<>(savedEpic.getSubtasksIds());
        assertArrayEquals(new int[]{}, intListToArr(epicSubtasksIds), "Эпик содержит некорректный набор id подзадач");

        // Создаём подзадачу 1, указываем им epicId, добавляем в менеджер
        Subtask subtask1 = new Subtask("Subtask addNewSubtask 1", "Subtask addNewSubtask description 1", TaskStatus.IN_PROGRESS);
        subtask1.setEpicId(epicId);
        int subtaskId1 = taskManager.addNewSubtask(subtask1);
        Subtask savedSubtask1 = taskManager.getSubtask(subtaskId1);

        savedEpic = taskManager.getEpic(epicId);
        epicSubtasksIds = savedEpic.getSubtasksIds();
        assertArrayEquals(new int[]{subtaskId1}, intListToArr(epicSubtasksIds), "Эпик содержит некорректный набор id подзадач");

        // Создаём подзадачу 2, указываем им epicId, добавляем в менеджер
        Subtask subtask2 = new Subtask("Subtask addNewSubtask 2", "Subtask addNewSubtask description 2", TaskStatus.IN_PROGRESS);
        subtask2.setEpicId(epicId);
        int subtaskId2 = taskManager.addNewSubtask(subtask2);
        Subtask savedSubtask2 = taskManager.getSubtask(subtaskId2);

        savedEpic = taskManager.getEpic(epicId);
        epicSubtasksIds = savedEpic.getSubtasksIds();
        assertArrayEquals(new int[]{subtaskId1, subtaskId2}, intListToArr(epicSubtasksIds), "Эпик содержит некорректный набор id подзадач");

        // Удаляем подзадачу 2
        taskManager.deleteSubtaskById(subtaskId2);

        savedEpic = taskManager.getEpic(epicId);
        epicSubtasksIds = savedEpic.getSubtasksIds();
        assertArrayEquals(new int[]{subtaskId1}, intListToArr(epicSubtasksIds), "Эпик содержит некорректный набор id подзадач");

        // Удаляем подзадачу 1
        taskManager.deleteSubtaskById(subtaskId1);

        savedEpic = taskManager.getEpic(epicId);
        epicSubtasksIds = savedEpic.getSubtasksIds();
        assertArrayEquals(new int[]{}, intListToArr(epicSubtasksIds), "Эпик содержит некорректный набор id подзадач");
    }

    private int[] intListToArr(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

}
