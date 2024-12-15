package entities;

import enums.TaskStatus;

public class Task {
    protected int id;
    protected String title;
    protected String description;

    protected TaskStatus status;

    public Task(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(Task task) {
        this.id = task.id;
        this.title = task.title;
        this.description = task.description;
        this.status = task.status;
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
        StringBuilder result = new StringBuilder("Task{")
                .append("id='").append(id).append("', ")
                .append("status='").append(status.name()).append("', ")
                .append("title=").append(title != null ? ("'" + title + "'") : "null").append(", ")
                .append("description.length=").append(description != null ? description.length() : 0)
                .append("}");
        return result.toString();
    }

}
