package interfaces;

import entities.Epic;
import entities.Subtask;
import entities.Task;

import java.util.List;

public interface TaskManager {
    int getNewId();

    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    int createTask(Task task);

    int createSubtask(Subtask subtask);

    int createEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    List<Subtask> getEpicSubtasks(int id);

    List<Subtask> getEpicSubtasks(Epic epic);

    void deleteEpicSubtasks(Epic epic);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
