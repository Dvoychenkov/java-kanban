package ru.yandex.practicum.taskmanager.entities;

import ru.yandex.practicum.taskmanager.enums.TaskStatus;
import ru.yandex.practicum.taskmanager.enums.TaskType;

public class Task {
    protected final int id;
    protected String title;
    protected String description;
    public TaskStatus status;
    protected TaskType type;

    public Task(int id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = TaskType.TASK;
    }

    public int getId() {
        return hashCode();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task thatTask = (Task) o;

        // Две задачи с одинаковым id должны выглядеть для менеджера как одна и та же.
        return id == thatTask.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {

        String result = "\nTask{" + "id='" + id + "', " +
                "type='" + type.name() + "', " +
                "status='" + status.name() + "', ";

        if(title != null) {
            result = result + "title=" + title + "', ";
        } else {
            result = result + "title=null, ";
        }

        if(description != null) {
            result = result + "description.length=" + description.length();
        } else {
            result = result + "description.length=0";
        }

        result = result + "}\n";
        return result;
    }

}
