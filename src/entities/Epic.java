package entities;

import enums.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksIds = new ArrayList<>();

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status);
    }

    public Epic(String title, String description, TaskStatus status, ArrayList<Integer> subtasksIds) {
        super(title, description, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, int[] subtasksIds) {
        this(title, description, status);

        for (int subTasksId : subtasksIds) {
            this.subtasksIds.add(subTasksId);
        }
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(ArrayList<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

}
