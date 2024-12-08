package ru.yandex.practicum.taskmanager.enums;

public enum TaskType {
    TASK,
    SUBTASK,
    EPIC;

    @Override
    public String toString() {
        String name = name().replaceAll("_", " ");
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
