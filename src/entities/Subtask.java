package entities;

import enums.TaskStatus;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, TaskStatus status) {
        super(title, description, status);
    }

    public Subtask(Subtask subtask) {
        super(subtask);
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

}
