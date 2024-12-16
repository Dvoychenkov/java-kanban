package entities;

import enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status);
    }

    public Epic(Epic epic) {
        super(epic);
        this.subtasksIds = epic.subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, List<Integer> subtasksIds) {
        super(title, description, status);
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
