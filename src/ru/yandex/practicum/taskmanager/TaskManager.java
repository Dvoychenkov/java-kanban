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
    private final HashMap<Integer, Subtask> subtasksIdsToTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epicsIdsToTasks = new HashMap<>();

    public int getNewId() {
        return idsCount++;
    }

    // Получение списка всех задач
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!tasksIdsToTasks.isEmpty()) {
            tasks.addAll(tasksIdsToTasks.values());
        }
        return tasks;
    }

    // Получение списка всех подзадач
    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (!subtasksIdsToTasks.isEmpty()) {
            subtasks.addAll(subtasksIdsToTasks.values());
        }
        return subtasks;
    }

    // Получение списка всех эпиков
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> epics = new ArrayList<>();
        if (!epicsIdsToTasks.isEmpty()) {
            epics.addAll(epicsIdsToTasks.values());
        }
        return epics;
    }


    // Удаление всех задач
    public void deleteAllTasks() {
        tasksIdsToTasks.clear();
    }

    // Удаление всех подзадач
    public void deleteAllSubtasks() {
        ArrayList<Subtask> subtasksToBeUpdated = new ArrayList<>(subtasksIdsToTasks.values());
        subtasksIdsToTasks.clear();

        for (Subtask subtask : subtasksToBeUpdated) {
            updateEpicDataBySubtask(subtask);
        }
    }

    // Удаление всех эпиков
    public void deleteAllEpics() {
        epicsIdsToTasks.clear();
    }


    // Получение задачи по идентификатору
    public Task getTaskById(int id) {
        return tasksIdsToTasks.getOrDefault(id, null);
    }

    // Получение подзадачи по идентификатору
    public Subtask getSubtaskById(int id) {
        return subtasksIdsToTasks.getOrDefault(id, null);
    }

    // Получение эпика по идентификатору
    public Epic getEpicById(int id) {
        return epicsIdsToTasks.getOrDefault(id, null);
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
            subtasksIdsToTasks.put(subtask.getId(), subtask);
            updateEpicDataBySubtask(subtask);
        }
    }

    // Создание эпика
    public void createEpic(Epic epic) {
        if (epic != null) {
            epicsIdsToTasks.put(epic.getId(), epic);
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
            if (subtasksIdsToTasks.containsKey(id)) {
                subtasksIdsToTasks.replace(id, subtask);
                updateEpicDataBySubtask(subtask);
            }
        }
    }

    // Обновление эпика
    public void updateEpic(Epic epic) {
        if (epic != null) {
            int id = epic.getId();
            if (epicsIdsToTasks.containsKey(id)) {
                epicsIdsToTasks.replace(id, epic);
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
        Subtask subtask = subtasksIdsToTasks.get(id);
        if (subtask != null) {
            subtasksIdsToTasks.remove(id);
            updateEpicDataBySubtask(subtask);
        }
    }

    // Удаление эпика по идентификатору
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToTasks.get(id);
        if (epic != null) {
            epicsIdsToTasks.remove(id);
        }
    }


    // Получение списка всех подзадач определённого эпика
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
            if (subtasksIdsToTasks.containsKey(subtasksId)) {
                Subtask subtask = subtasksIdsToTasks.get(subtasksId);
                if (subtask != null) {
                    subtasks.add(subtask);
                }
            }
        }
        return subtasks;
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

        // Добавление id подзадачи в список подзадач эпика, если такового ещё нет
        int currentSubtaskId = subtask.getId();
        boolean isSubtaskIdInEpic = false;
        ArrayList<Integer> subtasksIds = epic.getSubtasksIds();
        for (Integer subtaskId : subtasksIds) {
            if (currentSubtaskId == subtaskId) {
                isSubtaskIdInEpic = true;
                break;
            }
        }
        if (!isSubtaskIdInEpic) {
            subtasksIds.add(currentSubtaskId);
            epic.setSubtasksIds(subtasksIds);
        }

        updateEpicData(epic);
    }

    // Обновление эпика
    private void updateEpicData(Epic epic) {
        ArrayList<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);
        // Зануляем эпик, если у него нети подзадач
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

    // Если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
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
