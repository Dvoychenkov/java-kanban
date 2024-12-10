package ru.yandex.practicum.taskmanager;

import ru.yandex.practicum.taskmanager.entities.Epic;
import ru.yandex.practicum.taskmanager.entities.Subtask;
import ru.yandex.practicum.taskmanager.entities.Task;
import ru.yandex.practicum.taskmanager.enums.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int idsCount;

    private final HashMap<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasksIdsToSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> epicsIdsToEpics = new HashMap<>();

    public int getNewId() {
        return idsCount++;
    }

    // Получение списка всех задач
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasksIdsToTasks.values());
    }

    // Получение списка всех подзадач
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasksIdsToSubtasks.values());
    }

    // Получение списка всех эпиков
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicsIdsToEpics.values());
    }


    // Удаление всех задач
    public void deleteAllTasks() {
        tasksIdsToTasks.clear();
    }

    // Удаление всех подзадач
    public void deleteAllSubtasks() {
        ArrayList<Subtask> subtasksToBeUpdated = new ArrayList<>(subtasksIdsToSubtasks.values());
        subtasksIdsToSubtasks.clear();

        for (Subtask subtask : subtasksToBeUpdated) {
            updateEpicDataBySubtask(subtask);
        }
    }

    // Удаление всех эпиков
    public void deleteAllEpics() {
        for (Epic epic : epicsIdsToEpics.values()) {
            deleteEpicSubtasks(epic);
            updateEpicData(epic);
        }

        epicsIdsToEpics.clear();
    }


    // Получение задачи по идентификатору
    public Task getTaskById(int id) {
        return tasksIdsToTasks.getOrDefault(id, null);
    }

    // Получение подзадачи по идентификатору
    public Subtask getSubtaskById(int id) {
        return subtasksIdsToSubtasks.getOrDefault(id, null);
    }

    // Получение эпика по идентификатору
    public Epic getEpicById(int id) {
        return epicsIdsToEpics.getOrDefault(id, null);
    }


    // Создание задачи
    public void createTask(Task task) {
        if (task != null) {
            tasksIdsToTasks.put(task.getId(), task);
        }
    }

    // Создание подзадачи
    public void createSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasksIdsToSubtasks.put(subtask.getId(), subtask);
            updateEpicDataBySubtask(subtask);
        }
    }

    // Создание эпика
    public void createEpic(Epic epic) {
        if (epic != null) {
            epicsIdsToEpics.put(epic.getId(), epic);
            updateEpicData(epic);
        }
    }


    // Обновление задачи
    public void updateTask(Task task) {
        if (task != null) {
            int id = task.getId();
            if (tasksIdsToTasks.containsKey(id)) {
                tasksIdsToTasks.replace(id, task);
            }
        }
    }

    // Обновление подзадачи
    public void updateSubtask(Subtask subtask) {
        if (subtask != null) {
            int id = subtask.getId();
            if (subtasksIdsToSubtasks.containsKey(id)) {
                subtasksIdsToSubtasks.replace(id, subtask);
                updateEpicDataBySubtask(subtask);
            }
        }
    }

    // Обновление эпика
    public void updateEpic(Epic epic) {
        if (epic != null) {
            int id = epic.getId();
            if (epicsIdsToEpics.containsKey(id)) {
                epicsIdsToEpics.replace(id, epic);
                updateEpicData(epic);
            }
        }
    }


    // Удаление задачи по идентификатору
    public void deleteTaskById(int id) {
        Task task = tasksIdsToTasks.get(id);
        if (task != null) {
            tasksIdsToTasks.remove(id);
        }
    }

    // Удаление подзадачи по идентификатору
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasksIdsToSubtasks.get(id);
        if (subtask != null) {
            subtasksIdsToSubtasks.remove(id);
            updateEpicDataBySubtask(subtask);
        }
    }

    // Удаление эпика по идентификатору
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToEpics.get(id);
        if (epic != null) {
            epicsIdsToEpics.remove(id);
            deleteEpicSubtasks(epic);
            updateEpicData(epic);
        }
    }


    // Получение списка всех подзадач определённого эпика
    // Учитывается, что подзадача должна быть в списке подзадач
    public ArrayList<Subtask> getSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epic == null) {
            return subtasks;
        }

        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        if (subtasksIds.isEmpty()) {
            return subtasks;
        }

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

    // Удаление всех подзадач эпика
    private void deleteEpicSubtasks(Epic epic) {
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtasksIdsToSubtasks.remove(subtaskId);
        }
    }

    // Обновление эпика подзадачи
    private void updateEpicDataBySubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int epicId = subtask.getEpicId();

        Epic epic = getEpicById(epicId);
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

    // Обновление эпика
    private void updateEpicData(Epic epic) {
        ArrayList<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);

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
    private TaskStatus calcEpicStatus(ArrayList<Subtask> subtasksOfEpic) {
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
