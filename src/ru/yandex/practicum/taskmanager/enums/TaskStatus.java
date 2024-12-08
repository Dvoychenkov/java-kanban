package ru.yandex.practicum.taskmanager.enums;

public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE;

    @Override
    public String toString() {
        String name = name().replaceAll("_", " ");
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
