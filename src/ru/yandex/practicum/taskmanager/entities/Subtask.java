package ru.yandex.practicum.taskmanager.entities;

import ru.yandex.practicum.taskmanager.enums.TaskStatus;
import ru.yandex.practicum.taskmanager.enums.TaskType;

public class Subtask extends Task {
    private int parentTaskId;

    public Subtask(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
        this.type = TaskType.SUBTASK;
    }

    public Subtask(int id, String title, String description, TaskStatus status, int parentTaskId) {
        this(id, title, description, status);

        this.parentTaskId = parentTaskId;
    }

    public int getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(int parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

}
