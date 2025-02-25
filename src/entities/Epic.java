package entities;

import enums.TaskStatus;
import enums.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status, TaskType.EPIC);
    }

    public Epic(Epic epic) {
        this(epic.title, epic.description, epic.status);
        this.id = epic.id;
        this.subtasksIds = epic.subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, List<Integer> subtasksIds) {
        this(title, description, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, int[] subtasksIds) {
        this(title, description, status);

        for (int subTasksId : subtasksIds) {
            this.subtasksIds.add(subTasksId);
        }
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }
}
