package entities;

import enums.TaskStatus;
import enums.TaskType;

public class Task {
    protected final TaskType type;
    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus status;

    protected Task(String title, String description, TaskStatus status, TaskType type) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
    }

    public Task(String title, String description, TaskStatus status) {
        this(title, description, status, TaskType.TASK);
    }

    public Task(Task task) {
        this(task.title, task.description, task.status, TaskType.TASK);
        this.id = task.id;
    }

    public TaskType getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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
        return getId();
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", id, type, title, status, description);
    }
}
