package managers;

import interfaces.*;
import entities.*;
import enums.TaskStatus;
import utilities.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private int idsCount;

    private final HashMap<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasksIdsToSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epicsIdsToEpics = new HashMap<>();

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public int getNewId() {
        return idsCount++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasksIdsToTasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasksIdsToSubtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicsIdsToEpics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasksIdsToTasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        ArrayList<Subtask> subtasksToBeUpdated = new ArrayList<>(subtasksIdsToSubtasks.values());
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

        epicsIdsToEpics.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasksIdsToTasks.getOrDefault(id, null);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasksIdsToSubtasks.getOrDefault(id, null);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicsIdsToEpics.getOrDefault(id, null);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void createTask(Task task) {
        if (task != null) {
            tasksIdsToTasks.put(task.getId(), task);
        }
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasksIdsToSubtasks.put(subtask.getId(), subtask);
            updateEpicDataBySubtask(subtask);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic != null) {
            epicsIdsToEpics.put(epic.getId(), epic);
            updateEpicData(epic);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null) {
            int id = task.getId();
            if (tasksIdsToTasks.containsKey(id)) {
                tasksIdsToTasks.replace(id, task);
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            int id = subtask.getId();
            if (subtasksIdsToSubtasks.containsKey(id)) {
                subtasksIdsToSubtasks.replace(id, subtask);
                updateEpicDataBySubtask(subtask);
            }
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null) {
            int id = epic.getId();
            if (epicsIdsToEpics.containsKey(id)) {
                epicsIdsToEpics.replace(id, epic);
                updateEpicData(epic);
            }
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasksIdsToTasks.get(id);
        if (task != null) {
            tasksIdsToTasks.remove(id);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasksIdsToSubtasks.get(id);
        if (subtask != null) {
            subtasksIdsToSubtasks.remove(id);
            updateEpicDataBySubtask(subtask);
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToEpics.get(id);
        if (epic != null) {
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
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epic == null) {
            return subtasks;
        }

        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            return subtasks;
        }

        // Учитываем, что подзадача должна быть в списке подзадач
        for (Integer subtasksId : subtasksIds) {
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
            subtasksIdsToSubtasks.remove(subtaskId);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Обновление эпика подзадачи
    private void updateEpicDataBySubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();

        Epic epic = getEpic(epicId);
        if (epic == null) {
            return;
        }

        // Добавление id подзадачи в список подзадач эпика, если его ещё нет в нём
        int currentSubtaskId = subtask.getId();
        ArrayList<Integer> epicSubtasksIds = epic.getSubtasksIds();
        if (!epicSubtasksIds.contains(currentSubtaskId)) {
            epicSubtasksIds.add(currentSubtaskId);
        }

        updateEpicData(epic);
    }

    // Обновление данных эпика
    private void updateEpicData(Epic epic) {
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);

        // Если у эпика нет подзадач, то статус должен быть NEW
        if (subtasksOfEpic.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setSubtasksIds(new ArrayList<>());
            return;
        }

        // Обновление списка подзадач эпика
        ArrayList<Integer> epicSubtasksIds = new ArrayList<>();
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

    // Если у эпика все подзадачи имеют статус NEW, то статус должен быть NEW.
    // Если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    // Во всех остальных случаях статус должен быть IN_PROGRESS.
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
