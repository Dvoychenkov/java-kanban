package entities;

import enums.TaskStatus;
import enums.TaskType;
import exceptions.TaskStringParseException;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    protected final TaskType type;
    protected int id;
    protected String title;
    protected String description;
    protected TaskStatus status;
    protected LocalDateTime startTime;
    protected Duration duration = Duration.ZERO;

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
        this.startTime = task.startTime;
        this.duration = task.duration;
    }

    public Task(String title, String description, TaskStatus status, LocalDateTime startTime, Duration duration, TaskType type) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
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

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = (duration == null) ? Duration.ZERO : duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null || duration.isZero()) {
            return null;
        }
        return startTime.plusMinutes(duration.toMinutes());
    }

    // Проверяем пересечения отрезка выполнения текущей задачи и переданной
    public boolean intersectsByTimeIntervals(Task other) {
        if (other == null) return false;

        LocalDateTime thisEndTime = getEndTime();
        LocalDateTime otherStartTime = other.startTime;
        LocalDateTime otherEndTime = other.getEndTime();
        if (startTime == null || otherStartTime == null || thisEndTime == null || otherEndTime == null) return false;

        return startTime.isBefore(otherEndTime) && thisEndTime.isAfter(otherStartTime);
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
        return String.format("%d,%s,%s,%s,%s,,%d,%s",
                id, type, title, status, description, duration.toMinutes(), (startTime == null) ? "" : startTime);
    }

    public static Task fromString(String line) {
        String[] fields = line.trim().split(",");
        if (fields.length < 5) {
            throw new TaskStringParseException(String.format("Некорректная длина строки: %s", line));
        }

        int id;
        try {
            id = Integer.parseInt(fields[0]);
        } catch (NumberFormatException ex) {
            throw new TaskStringParseException(String.format("Некорректный id строки: %s", line));
        }

        TaskType type = TaskType.valueOf(fields[1]);
        String title = fields[2];
        TaskStatus status = fields[3].isEmpty() ? TaskStatus.NEW : TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(title, description, status);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description, status);
                epic.setId(id);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, status);
                subtask.setId(id);

                if (fields.length > 5 && !fields[5].isEmpty()) {
                    int epicId;
                    try {
                        epicId = Integer.parseInt(fields[5]);
                    } catch (NumberFormatException ex) {
                        throw new TaskStringParseException(String.format("Некорректный epic id строки: %s", line));
                    }
                    subtask.setEpicId(epicId);
                }

                return subtask;
            default:
                throw new TaskStringParseException(String.format("Неизвестный тип задачи: %s", type));
        }
    }
}
