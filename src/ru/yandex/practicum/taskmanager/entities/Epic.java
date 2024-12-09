package ru.yandex.practicum.taskmanager.entities;

import ru.yandex.practicum.taskmanager.enums.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksIds = new ArrayList<>();

    public Epic(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

    public Epic(int id, String title, String description, TaskStatus status, ArrayList<Integer> subtasksIds) {
        super(id, title, description, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic(int id, String title, String description, TaskStatus status, int[] subtasksIds) {
        this(id, title, description, status);

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
