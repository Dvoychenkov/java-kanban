package ru.yandex.practicum.taskmanager.interfaces;

import ru.yandex.practicum.taskmanager.entities.Epic;
import ru.yandex.practicum.taskmanager.entities.Subtask;
import ru.yandex.practicum.taskmanager.entities.Task;

import java.util.ArrayList;

public interface TaskManager {
    int getNewId();

    // Получение списка всех задач
    ArrayList<Task> getAllTasks();

    // Получение списка всех подзадач
    ArrayList<Subtask> getAllSubtasks();

    // Получение списка всех эпиков
    ArrayList<Epic> getAllEpics();

    // Удаление всех задач
    void deleteAllTasks();

    // Удаление всех подзадач
    void deleteAllSubtasks();

    // Удаление всех эпиков
    void deleteAllEpics();

    // Получение задачи по идентификатору
    Task getTaskById(int id);

    // Получение подзадачи по идентификатору
    Subtask getSubtaskById(int id);

    // Получение эпика по идентификатору
    Epic getEpicById(int id);

    // Создание задачи
    void createTask(Task task);

    // Создание подзадачи
    void createSubtask(Subtask subtask);

    // Создание эпика
    void createEpic(Epic epic);

    // Обновление задачи
    void updateTask(Task task);

    // Обновление подзадачи
    void updateSubtask(Subtask subtask);

    // Обновление эпика
    void updateEpic(Epic epic);

    // Удаление задачи по идентификатору
    void deleteTaskById(int id);

    // Удаление подзадачи по идентификатору
    void deleteSubtaskById(int id);

    // Удаление эпика по идентификатору
    void deleteEpicById(int id);

    // Получение списка всех подзадач определённого эпика
    ArrayList<Subtask> getSubtasksOfEpic(Epic epic);

    // Удаление всех подзадач эпика
    void deleteEpicSubtasks(Epic epic);
}
