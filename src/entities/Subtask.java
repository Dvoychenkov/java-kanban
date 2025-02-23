package entities;

import enums.TaskStatus;
import enums.TaskType;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, TaskStatus status) {
        super(title, description, status, TaskType.SUBTASK);
    }

    public Subtask(Subtask subtask) {
        this(subtask.title, subtask.description, subtask.status);
        this.id = subtask.id;
        this.epicId = subtask.epicId;
    }

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        this(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", id, type, title, status, description, epicId);
    }
}
