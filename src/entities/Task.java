package entities;

import enums.TaskStatus;

public class Task {
    protected final int id;
    protected String title;
    protected String description;

    protected TaskStatus status;

    public Task(int id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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

        String result = "Task{" + "id='" + id + "', " +
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

        result = result + "}";
        return result;
    }

}
