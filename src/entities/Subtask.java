package entities;

import enums.TaskStatus;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

    public Subtask(int id, String title, String description, TaskStatus status, int epicId) {
        this(id, title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

}
