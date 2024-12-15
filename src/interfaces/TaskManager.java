package interfaces;

import entities.Epic;
import entities.Subtask;
import entities.Task;

import java.util.List;

public interface TaskManager {
    int getNewId();

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    void createTask(Task task);

    void createSubtask(Subtask subtask);

    void createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void deleteTaskById(int id);

    void deleteSubtaskById(int id);

    void deleteEpicById(int id);

    List<Subtask> getSubtasksOfEpic(int id);

    List<Subtask> getSubtasksOfEpic(Epic epic);

    void deleteEpicSubtasks(Epic epic);

    List<Task> getHistory();
}
