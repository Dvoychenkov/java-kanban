package ru.yandex.practicum.taskmanager.entities;

import ru.yandex.practicum.taskmanager.enums.TaskStatus;
import ru.yandex.practicum.taskmanager.enums.TaskType;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTasksIds = new ArrayList<>();

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
        // Пользователь не должен иметь возможности поменять статус эпика самостоятельно.
        setStatus(TaskStatus.NEW);
        this.type = TaskType.EPIC;
    }

    public Epic(int id, String title, String description, TaskStatus status, int[] subTasksIds) {
        this(id, title, description, status);

        for (int subTasksId : subTasksIds) {
            this.subTasksIds.add(subTasksId);
        }
    }

    public Epic(int id, String title, String description, TaskStatus status, ArrayList<Integer> subTasksIds) {
        super(id, title, description, status);
        this.subTasksIds = subTasksIds;
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void setSubTasksIds(ArrayList<Integer> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

}
