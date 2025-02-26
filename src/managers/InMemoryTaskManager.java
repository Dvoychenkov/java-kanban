package managers;

import exceptions.ManagerSaveException;
import interfaces.*;
import entities.*;
import utilities.Managers;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final HistoryManager historyManager;
    private int idsCount = 1;

    private final Map<Integer, Task> tasksIdsToTasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasksIdsToSubtasks = new HashMap<>();
    private final Map<Integer, Epic> epicsIdsToEpics = new HashMap<>();
    private final Set<Task> sortedByStartTimeTasksAndSubtasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

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
        tasksIdsToTasks.values().stream()
                .peek(task -> historyManager.remove(task.getId()))
                .forEach(sortedByStartTimeTasksAndSubtasks::remove);
        tasksIdsToTasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        List<Subtask> allSubtasks = new ArrayList<>(subtasksIdsToSubtasks.values());
        subtasksIdsToSubtasks.clear();
        allSubtasks.stream()
                .peek(subtask -> historyManager.remove(subtask.getId()))
                .peek(sortedByStartTimeTasksAndSubtasks::remove)
                .peek(this::updateEpicDataBySubtask)
                .peek(subtask -> subtask.setId(0)) // Удаляемые подзадачи не должны хранить внутри себя старые id
                .forEach(subtask -> subtask.setEpicId(0));
    }

    @Override
    public void deleteAllEpics() {
        epicsIdsToEpics.values().stream()
                .peek(this::deleteEpicSubtasks)
                .peek(this::updateEpicData)
                .forEach(epic -> historyManager.remove(epic.getId()));
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
        if (task.getStartTime() == null) {
            return id;
        }

        // Проверяем на пересечение с задачами и подзадачами, добавляем в отсортированные, если их нет
        boolean hasIntersection = getPrioritizedTasks().stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(task));
        if (hasIntersection) {
            throw new ManagerSaveException("Задача пересекается по времени выполнения с уже существующими");
        }
        sortedByStartTimeTasksAndSubtasks.add(new Task(task));

        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }

        int id = getNewId();
        subtask.setId(id);
        subtasksIdsToSubtasks.put(id, new Subtask(subtask));
        updateEpicDataBySubtask(subtask);
        if (subtask.getStartTime() == null) {
            return id;
        }

        // Проверяем на пересечение с задачами и подзадачами, добавляем в отсортированные, если их нет
        boolean hasIntersection = getPrioritizedTasks().stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(subtask));
        if (hasIntersection) {
            throw new ManagerSaveException("Подзадача пересекается по времени выполнения с уже существующими");
        }
        sortedByStartTimeTasksAndSubtasks.add(new Subtask(subtask));

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
        int id = task.getId();

        Task oldTask = null;
        if (tasksIdsToTasks.containsKey(id)) {
            oldTask = tasksIdsToTasks.get(id);
            tasksIdsToTasks.replace(id, new Task(task));
        }

        // Обновляем задачу в отсортированных - удаляем старый объект, если он был и добавляем новый, если подходит по условиям
        if (oldTask != null) {
            sortedByStartTimeTasksAndSubtasks.remove(oldTask);
        }
        if (task.getStartTime() == null) {
            return;
        }

        // Проверяем на пересечение с задачами и подзадачами, добавляем в отсортированные, если их нет
        boolean hasIntersection = getPrioritizedTasks().stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(task));
        if (!hasIntersection) {
            sortedByStartTimeTasksAndSubtasks.add(new Task(task));
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        int id = subtask.getId();

        Subtask oldSubtask = null;
        if (subtasksIdsToSubtasks.containsKey(id)) {
            updateEpicDataBySubtask(subtask);
            oldSubtask = subtasksIdsToSubtasks.get(id);
            subtasksIdsToSubtasks.replace(id, new Subtask(subtask));
        }

        // Обновляем подзадачу в отсортированных - удаляем старый объект, если он был и добавляем новый, если подходит по условиям
        if (oldSubtask != null) {
            sortedByStartTimeTasksAndSubtasks.remove(oldSubtask);
        }
        if (subtask.getStartTime() == null) {
            return;
        }

        // Проверяем на пересечение с задачами и подзадачами, добавляем в отсортированные, если их нет
        boolean hasIntersection = getPrioritizedTasks().stream()
                .anyMatch(taskOrSubtask -> taskOrSubtask.intersectsByTimeIntervals(subtask));
        if (!hasIntersection) {
            sortedByStartTimeTasksAndSubtasks.add(new Subtask(subtask));
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
        Task task = tasksIdsToTasks.get(id);
        if (task == null) {
            return;
        }
        historyManager.remove(id);
        tasksIdsToTasks.remove(id);
        sortedByStartTimeTasksAndSubtasks.remove(task);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasksIdsToSubtasks.get(id);
        if (subtask == null) {
            return;
        }
        historyManager.remove(id);
        subtasksIdsToSubtasks.remove(id);
        sortedByStartTimeTasksAndSubtasks.remove(subtask);
        updateEpicDataBySubtask(subtask);

        // Удаляемые подзадачи не должны хранить внутри себя старые id
        subtask.setId(0);
        subtask.setEpicId(0);
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epicsIdsToEpics.get(id);
        if (epic == null) {
            return;
        }
        historyManager.remove(id);
        epicsIdsToEpics.remove(id);
        deleteEpicSubtasks(epic);
        updateEpicData(epic);
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

    @Override
    public void deleteEpicSubtasks(Epic epic) {
        epic.getSubtasksIds().stream()
                .map(subtasksIdsToSubtasks::get)
                .filter(Objects::nonNull)
                .peek(subtask -> historyManager.remove(subtask.getId()))
                .peek(subtask -> subtasksIdsToSubtasks.remove(subtask.getId()))
                .peek(sortedByStartTimeTasksAndSubtasks::remove)
                .peek(subtask -> subtask.setId(0)) // Удаляемые подзадачи не должны хранить внутри себя старые id
                .forEach(subtask -> subtask.setEpicId(0));
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

    private void updateEpicData(Epic epic) {
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);
        epic.updateData(subtasksOfEpic);
    }

    private List<Task> getPrioritizedTasks() {
        return new ArrayList<>(sortedByStartTimeTasksAndSubtasks);
    }
}
