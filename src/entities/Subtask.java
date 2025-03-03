package entities;

import enums.TaskStatus;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String title, String description, TaskStatus status) {
        super(title, description, status, TaskType.SUBTASK);
    }

    public Subtask(String title, String description, TaskStatus status, int epicId) {
        this(title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this(title, description, status);
        this.duration = duration;
        this.startTime = startTime;
    }

    public Subtask(String title, String description, TaskStatus status, int epicId, LocalDateTime startTime, Duration duration) {
        this(title, description, status);
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Subtask(Subtask subtask) {
        this(subtask.title, subtask.description, subtask.status);
        this.id = subtask.id;
        this.epicId = subtask.epicId;
        this.startTime = subtask.startTime;
        this.duration = subtask.duration;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d,%d,%s",
                id, type, title, status, description, epicId, duration.toMinutes(), (startTime == null) ? "" : startTime);
    }
}
