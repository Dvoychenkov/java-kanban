package ru.yandex.practicum.taskmanager;

import ru.yandex.practicum.taskmanager.entities.Epic;
import ru.yandex.practicum.taskmanager.entities.Subtask;
import ru.yandex.practicum.taskmanager.entities.Task;
import ru.yandex.practicum.taskmanager.enums.TaskStatus;
import ru.yandex.practicum.taskmanager.enums.TaskType;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int tasksIdsCount;
    private static final HashMap<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private static final HashMap<TaskType, HashMap<Integer, Task>> taskTypesToTasksIdsToTasks = new HashMap<>();

    public static int getNewTaskId() {
        return tasksIdsCount++;
    }

    // Получение списка всех задач
    public static ArrayList<Task> getAllTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        if (tasksIdsToTasks.isEmpty()) {
            return tasks;
        }

        for (Task task : tasksIdsToTasks.values()) {
            tasks.add(task);
        }
        return tasks;
    }

    // Получение списка всех задач по типу
    public static ArrayList<Task> getTasksByTaskType(TaskType taskType) {
        ArrayList<Task> tasks = new ArrayList<>();
        if (tasksIdsToTasks.isEmpty()) {
            return tasks;
        }

        if (!taskTypesToTasksIdsToTasks.containsKey(taskType)) {
            return tasks;
        }
        HashMap<Integer, Task> tasksIdsToTasksByType = taskTypesToTasksIdsToTasks.get(taskType);

        for (Task task : tasksIdsToTasksByType.values()) {
            tasks.add(task);
        }
        return tasks;
    }

    // Получение списка задач по id
    public static ArrayList<Task> getTasksByTasksIds(ArrayList<Integer> tasksIds) {
        ArrayList<Task> tasks = new ArrayList<>();
        if (tasksIdsToTasks.isEmpty()) {
            return tasks;
        }

        for (Integer taskId : tasksIds) {
            if (!tasksIdsToTasks.containsKey(taskId)){
                continue;
            }
            Task task = tasksIdsToTasks.get(taskId);

            if (task == null) {
                continue;
            }
            tasks.add(task);
        }

        return tasks;
    }

    // Удаление всех задач
    public static void deleteAllTasks() {
        ArrayList<Task> tasksToBeUpdated = new ArrayList<>();
        for (Task task : tasksIdsToTasks.values()) {
            tasksToBeUpdated.add(task);
        }

        tasksIdsToTasks.clear();
        taskTypesToTasksIdsToTasks.clear();

        for (Task task : tasksToBeUpdated) {
            TaskManager.updateData(task);
        }
    }

    // Удаление всех задач по типу
    public static void deleteTasks(TaskType taskType) {
        if (!taskTypesToTasksIdsToTasks.containsKey(taskType)) {
            return;
        }
        HashMap<Integer, Task> tasksIdsToTasksByType = taskTypesToTasksIdsToTasks.get(taskType);

        // Удаляем в общем списке
        ArrayList<Task> tasksToBeUpdated = new ArrayList<>();
        for (Task task : tasksIdsToTasksByType.values()) {
            int taskId = task.getId();
            tasksToBeUpdated.add(task);
            tasksIdsToTasks.remove(taskId);
        }

        // Удаляем в списке по типу
        tasksIdsToTasksByType.clear();

        for (Task task : tasksToBeUpdated) {
            TaskManager.updateData(task);
        }
    }

    // Получение задачи по идентификатору
    public static Task getTask(int taskId) {
        return tasksIdsToTasks.get(taskId);
    }

    // Создание задачи
    public static void createTask(Task task) {
        // Создаем в общем списке
        if (task == null) {
            return;
        }
        int taskId = task.getId();
        tasksIdsToTasks.put(taskId, task);

        // Создаем в списке по типу
        TaskType taskType = task.getTaskType();
        if (!taskTypesToTasksIdsToTasks.containsKey(taskType)) {
            taskTypesToTasksIdsToTasks.put(taskType, new HashMap<>());
        }
        HashMap<Integer, Task> tasksIdsToTasksByType = taskTypesToTasksIdsToTasks.get(taskType);
        tasksIdsToTasksByType.put(taskId, task);

        TaskManager.updateData(task);
    }

    // Обновление задачи
    public static void updateTask(Task task) {
        if (task == null) {
            return;
        }

        // Обновляем в общем списке
        int taskId = task.getId();
        if (!tasksIdsToTasks.containsKey(taskId)) {
            return;
        }
        tasksIdsToTasks.replace(taskId, task);

        // Обновляем в списке по типу
        TaskType taskType = task.getTaskType();
        if (!taskTypesToTasksIdsToTasks.containsKey(taskType)) {
            taskTypesToTasksIdsToTasks.put(taskType, new HashMap<>());
        }
        HashMap<Integer, Task> tasksIdsToTasksByType = taskTypesToTasksIdsToTasks.get(taskType);
        tasksIdsToTasksByType.replace(taskId, task);

        TaskManager.updateData(task);
    }

    // Удаление задачи
    public static void deleteTask(int taskId) {
        // Удаляем из общего списка
        Task task = tasksIdsToTasks.get(taskId);
        if (task == null) {
            return;
        }
        tasksIdsToTasks.remove(taskId);

        // Удаляем из списка по типу
        TaskType taskType = task.getTaskType();
        if (taskTypesToTasksIdsToTasks.containsKey(taskType)) {
            HashMap<Integer, Task> tasksIdsToTasksByType = taskTypesToTasksIdsToTasks.get(taskType);
            tasksIdsToTasksByType.remove(taskId);
        }

        TaskManager.updateData(task);
    }

    // Получение списка всех подзадач для задачи
    public static ArrayList<Task> getSubTasks(Task task) {
        ArrayList<Task> subTasksForParent = new ArrayList<>();
        if (task == null || task.getTaskType() != TaskType.EPIC) {
            return subTasksForParent;
        }
        Epic epic = (Epic) task;

        ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
        if (subTasksIds.isEmpty()) {
            return subTasksForParent;
        }

        for (Task taskToBeProcessed : TaskManager.getTasksByTasksIds(subTasksIds)) {
            if (taskToBeProcessed.getTaskType() != TaskType.SUBTASK) {
                continue;
            }
            subTasksForParent.add(taskToBeProcessed);
        }

        return subTasksForParent;
    }

    private static void updateData(Task task) {
        if (task == null) {
            return;
        }

        switch (task.getTaskType()) {
            // Пробуем найти предка и запустить для него обновление данных
            case SUBTASK:
                Subtask subtask = (Subtask) task;
                int parentTaskId = subtask.getParentTaskId();

                Epic parentTask = (Epic) TaskManager.getTask(parentTaskId);
                if(parentTask == null || parentTask.getTaskType() != TaskType.EPIC) {
                    return;
                }
                TaskManager.updateData(parentTask);
                break;

            // Если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
            // Если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
            // Во всех остальных случаях статус должен быть IN_PROGRESS.
            case EPIC:
                Epic epic = (Epic) task;

                ArrayList<Task> subTasksForParent = TaskManager.getSubTasks(epic);

                if (subTasksForParent.isEmpty()) {
                    epic.setStatus(TaskStatus.NEW);
                    return;
                }

                TaskStatus taskStatusToBeSet = TaskStatus.IN_PROGRESS;
                boolean hasInProgress = false;
                boolean hasNew = false;
                boolean hasDone = false;
                for (Task subTask : subTasksForParent) {
                    switch (subTask.status) {
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
                        break;
                    }
                }

                if (hasDone) {
                    taskStatusToBeSet = TaskStatus.DONE;
                } else if (hasNew) {
                    taskStatusToBeSet = TaskStatus.NEW;
                }

                epic.setStatus(taskStatusToBeSet);
                break;

            default:
                break;
        }

    }

}
