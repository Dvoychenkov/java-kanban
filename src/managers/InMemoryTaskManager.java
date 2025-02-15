package managers;

import interfaces.*;
import entities.*;
import enums.TaskStatus;
import utilities.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private int idsCount = 1;

    private final Map<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasksIdsToSubtasks = new HashMap<>();
    private final Map<Integer, Epic> epicsIdsToEpics = new HashMap<>();

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int getNewId() {
        return idsCount++;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasksIdsToTasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasksIdsToSubtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicsIdsToEpics.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : tasksIdsToTasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasksIdsToTasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        List<Subtask> subtasksToBeUpdated = new ArrayList<>(subtasksIdsToSubtasks.values());

        for (Integer subtaskId : subtasksIdsToSubtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasksIdsToSubtasks.clear();

        for (Subtask subtask : subtasksToBeUpdated) {
            updateEpicDataBySubtask(subtask);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epicsIdsToEpics.values()) {
            deleteEpicSubtasks(epic);
            updateEpicData(epic);
        }

        for (Integer epicId : epicsIdsToEpics.keySet()) {
            historyManager.remove(epicId);
        }
        epicsIdsToEpics.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasksIdsToTasks.getOrDefault(id, null);
        if (task == null) {
            return null;
        }
        historyManager.add(new Task(task));
        return new Task(task);
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasksIdsToSubtasks.getOrDefault(id, null);
        if (subtask == null) {
            return null;
        }
        historyManager.add(new Subtask(subtask));
        return new Subtask(subtask);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicsIdsToEpics.getOrDefault(id, null);
        if (epic == null) {
            return null;
        }
        historyManager.add(new Epic(epic));
        return new Epic(epic);
    }

    @Override
    public int addNewTask(Task task) {
        if (task == null) {
            return -1;
        }
        int id = getNewId();
        task.setId(id);
        tasksIdsToTasks.put(id, new Task(task));
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }
        int id = getNewId();
        subtask.setId(id);
        updateEpicDataBySubtask(subtask);
        subtasksIdsToSubtasks.put(id, new Subtask(subtask));
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        int id = getNewId();
        epic.setId(id);
        updateEpicData(epic);
        epicsIdsToEpics.put(id, new Epic(epic));
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (task != null) {
            int id = task.getId();
            if (tasksIdsToTasks.containsKey(id)) {
                tasksIdsToTasks.replace(id, new Task(task));
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            int id = subtask.getId();
            if (subtasksIdsToSubtasks.containsKey(id)) {
                updateEpicDataBySubtask(subtask);
                subtasksIdsToSubtasks.replace(id, new Subtask(subtask));
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null) {
            int id = epic.getId();
            if (epicsIdsToEpics.containsKey(id)) {
                updateEpicData(epic);
                epicsIdsToEpics.replace(id, new Epic(epic));
            }
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasksIdsToTasks.get(id);
        if (task != null) {
            historyManager.remove(id);
            tasksIdsToTasks.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasksIdsToSubtasks.get(id);
        if (subtask != null) {
            historyManager.remove(id);
            subtasksIdsToSubtasks.remove(id);
            updateEpicDataBySubtask(subtask);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToEpics.get(id);
        if (epic != null) {
            historyManager.remove(id);
            epicsIdsToEpics.remove(id);
            deleteEpicSubtasks(epic);
            updateEpicData(epic);
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int id) {
        return getSubtasksOfEpic(getEpic(id));
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(Epic epic) {
        if (epic == null || epic.getSubtasksIds().isEmpty()) {
            return new ArrayList<>();
        }

        // Учитываем, что подзадача должна быть в списке подзадач
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtasksId : epic.getSubtasksIds()) {
            if (subtasksIdsToSubtasks.containsKey(subtasksId)) {
                Subtask subtask = subtasksIdsToSubtasks.get(subtasksId);
                if (subtask != null) {
                    subtasks.add(subtask);
                }
            }
        }
        return subtasks;
    }

    @Override
    public void deleteEpicSubtasks(Epic epic) {
        for (Integer subtaskId : epic.getSubtasksIds()) {
            historyManager.remove(subtaskId);
            subtasksIdsToSubtasks.remove(subtaskId);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Обновление эпика подзадачи:
    // 1. Пробуем получить epic на основе данных subtask, убеждаемся, что он есть в списке эпиков.
    // 2. Добавляем id subtask в список связанных подзадач эпика, если его там нет.
    // 3. Запускаем процесс обновления данных для самого эпика.
    private void updateEpicDataBySubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();

        Epic epic = epicsIdsToEpics.get(epicId);
        if (epic == null) {
            subtask.setEpicId(0);
            return;
        }

        // Добавление id подзадачи в список подзадач эпика, если его ещё нет в нём
        int currentSubtaskId = subtask.getId();
        List<Integer> epicSubtasksIds = epic.getSubtasksIds();
        if (!epicSubtasksIds.contains(currentSubtaskId)) {
            epicSubtasksIds.add(currentSubtaskId);
        }

        updateEpicData(epic);
    }

    // Обновление данных эпика:
    // 1. Вычисляем и актуализируем статус эпика
    // 2. Обновляем список подзадач эпика
    private void updateEpicData(Epic epic) {
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);

        // Если у эпика нет подзадач, то статус должен быть NEW
        if (subtasksOfEpic.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setSubtasksIds(new ArrayList<>());
            return;
        }

        // Обновление списка подзадач эпика
        List<Integer> epicSubtasksIds = new ArrayList<>();
        for (Subtask epicsSubtask : subtasksOfEpic) {
            int epicSubtaskId = epicsSubtask.getId();
            epicSubtasksIds.add(epicSubtaskId);
        }
        epic.setSubtasksIds(epicSubtasksIds);

        // Обновление статуса для эпика
        // Пользователь не должен иметь возможности поменять статус эпика самостоятельно.
        TaskStatus statusToBeSet = calcEpicStatus(subtasksOfEpic);
        epic.setStatus(statusToBeSet);
    }

    // Вычисление статуса для эпика согласно условиям:
    // - Если у эпика все подзадачи имеют статус NEW, то статус должен быть NEW.
    // - Если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    // - Во всех остальных случаях статус должен быть IN_PROGRESS.
    private TaskStatus calcEpicStatus(List<Subtask> subtasksOfEpic) {
        TaskStatus statusToBeSet = TaskStatus.IN_PROGRESS;
        boolean hasInProgress = false;
        boolean hasNew = false;
        boolean hasDone = false;
        for (Task subTask : subtasksOfEpic) {
            switch (subTask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
            }

            if (hasInProgress || (hasNew && hasDone)) {
                return statusToBeSet;
            }
        }

        if (hasDone) {
            statusToBeSet = TaskStatus.DONE;
        } else if (hasNew) {
            statusToBeSet = TaskStatus.NEW;
        }
        return statusToBeSet;
    }

}
