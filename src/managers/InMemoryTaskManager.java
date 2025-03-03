package managers;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import exceptions.NotFoundException;
import exceptions.TaskIntersectionException;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import utilities.Managers;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private int idsCount = 1;

    private final Map<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasksIdsToSubtasks = new HashMap<>();
    private final Map<Integer, Epic> epicsIdsToEpics = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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

    // Зачищаем информацию о задачах во всех структурах
    @Override
    public void deleteAllTasks() {
        tasksIdsToTasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        });
        tasksIdsToTasks.clear();
    }

    // Зачищаем информацию о подзадачах во всех структурах, затем актуализируем данные эпиков
    @Override
    public void deleteAllSubtasks() {
        subtasksIdsToSubtasks.values().forEach(subtask -> {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        });
        subtasksIdsToSubtasks.clear();

        getEpics().forEach(this::updateEpic);
    }

    // Зачищаем информацию об эпиках во всех структурах и удаляем подзадачи эпиков
    @Override
    public void deleteAllEpics() {
        epicsIdsToEpics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            deleteEpicSubtasks(epic);
        });
        epicsIdsToEpics.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasksIdsToTasks.getOrDefault(id, null);
        if (task == null) {
//            return null;
            throw new NotFoundException(String.format("Задача с id '%d' не найдена", id));
        }
        historyManager.add(task);
        return new Task(task);
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasksIdsToSubtasks.getOrDefault(id, null);
        if (subtask == null) {
//            return null;
            throw new NotFoundException(String.format("Подзадача с id '%d' не найдена", id));
        }
        historyManager.add(subtask);
        return new Subtask(subtask);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicsIdsToEpics.getOrDefault(id, null);
        if (epic == null) {
//            return null;
            throw new NotFoundException(String.format("Эпик с id '%d' не найден", id));
        }
        historyManager.add(epic);
        return new Epic(epic);
    }

    @Override
    public int addNewTask(Task task) {
        if (task == null) {
            return -1;
        }

        // Проверяем на пересечение с задачами и подзадачами
        boolean hasIntersection = prioritizedTasks.stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(task));
        if (hasIntersection) {
            throw new TaskIntersectionException("Задача пересекается по времени выполнения с уже существующими");
        }

        int id = getNewId();
        task.setId(id);
        tasksIdsToTasks.put(id, new Task(task));
        if (task.getStartTime() != null) {
            prioritizedTasks.add(new Task(task));
        }
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }

        // Проверяем на пересечение с задачами и подзадачами
        boolean hasIntersection = prioritizedTasks.stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(subtask));
        if (hasIntersection) {
            throw new TaskIntersectionException("Подзадача пересекается по времени выполнения с уже существующими");
        }

        int id = getNewId();
        subtask.setId(id);
        subtasksIdsToSubtasks.put(id, new Subtask(subtask));
        updateEpicDataBySubtask(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(new Subtask(subtask));
        }
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
        if (task == null) {
            return;
        }

        // Проверяем на пересечение с задачами и подзадачами
        boolean hasIntersection = prioritizedTasks.stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(task));
        if (hasIntersection) {
            throw new TaskIntersectionException("Задача пересекается по времени выполнения с уже существующими");
        }

        int id = task.getId();
        Task oldTask = null;
        if (tasksIdsToTasks.containsKey(id)) {
            oldTask = tasksIdsToTasks.get(id);
            tasksIdsToTasks.replace(id, new Task(task));
        }

        // Обновляем задачу в отсортированных - удаляем старый объект при наличии и добавляем новый
        // Проверка на наличие времени для того, чтобы не сломался компаратор дерева
        if (oldTask != null && oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        if (task.getStartTime() != null) {
            prioritizedTasks.add(new Task(task));
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }

        // Проверяем на пересечение с задачами и подзадачами
        boolean hasIntersection = prioritizedTasks.stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(subtask));
        if (hasIntersection) {
            throw new TaskIntersectionException("Задача пересекается по времени выполнения с уже существующими");
        }

        int id = subtask.getId();
        Subtask oldSubtask = null;
        if (subtasksIdsToSubtasks.containsKey(id)) {
            oldSubtask = subtasksIdsToSubtasks.get(id);
            subtasksIdsToSubtasks.replace(id, new Subtask(subtask));
            updateEpicDataBySubtask(subtask);
        }

        // Обновляем подзадачу в отсортированных - удаляем старый объект при наличии и добавляем новый
        // Проверка на наличие времени для того, чтобы не сломался компаратор дерева
        if (oldSubtask != null && oldSubtask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubtask);
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(new Subtask(subtask));
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        int id = epic.getId();
        if (epicsIdsToEpics.containsKey(id)) {
            updateEpicData(epic);
            epicsIdsToEpics.replace(id, new Epic(epic));
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasksIdsToTasks.remove(id);
        if (task == null) {
            return;
        }
        historyManager.remove(id);
        prioritizedTasks.remove(task);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasksIdsToSubtasks.remove(id);
        if (subtask == null) {
            return;
        }
        historyManager.remove(id);
        prioritizedTasks.remove(subtask);
        updateEpicDataBySubtask(subtask);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToEpics.remove(id);
        if (epic == null) {
            return;
        }
        historyManager.remove(id);
        deleteEpicSubtasks(epic);
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
        return epic.getSubtasksIds().stream()
                .map(subtasksIdsToSubtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Зачищаем информацию о подзадачах эпика во всех структурах
    @Override
    public void deleteEpicSubtasks(Epic epic) {
        epic.getSubtasksIds().stream()
                .map(subtasksIdsToSubtasks::remove)
                .filter(Objects::nonNull)
                .forEach(subtask -> {
                    historyManager.remove(subtask.getId());
                    prioritizedTasks.remove(subtask);
                });
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
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

    private void updateEpicData(Epic epic) {
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);
        epic.updateData(subtasksOfEpic);
    }
}
