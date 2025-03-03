package entities;

import enums.TaskStatus;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Epic extends Task {
    private List<Integer> subtasksIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description, TaskStatus status) {
        super(title, description, status, TaskType.EPIC);
    }

    public Epic(Epic epic) {
        this(epic.title, epic.description, epic.status);
        this.id = epic.id;
        setSubtasksIds(epic.subtasksIds);
        this.startTime = epic.startTime;
        this.duration = epic.duration;
        this.endTime = epic.endTime;
    }

    public Epic(String title, String description, TaskStatus status, List<Integer> subtasksIds) {
        this(title, description, status);
        setSubtasksIds(subtasksIds);
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        if (subtasksIds != null) {
            this.subtasksIds = new ArrayList<>(subtasksIds);
        } else {
            this.subtasksIds = new ArrayList<>();
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Обновление данных эпика:
    // 1. Обновляем список подзадач эпика
    // 2. Вычисляем и актуализируем статус эпика
    // 3. Обновляем временные параметры эпика
    public void updateData(List<Subtask> subtasksOfEpic) {
        // Если у эпика нет подзадач, то статус должен быть NEW
        if (subtasksOfEpic.isEmpty()) {
            status = TaskStatus.NEW;
            subtasksIds = new ArrayList<>();
            startTime = null;
            endTime = null;
            duration = Duration.ZERO;
            return;
        }

        // Обновление списка подзадач эпика
        List<Integer> epicSubtasksIds = subtasksOfEpic.stream()
                .map(Subtask::getId)
                .collect(Collectors.toList());

        setSubtasksIds(epicSubtasksIds);
        calcEpicStatus(subtasksOfEpic);
        calcEpicTimes(subtasksOfEpic);
    }

    // Вычисление статуса для эпика согласно условиям:
    // - Если у эпика все подзадачи имеют статус NEW, то статус должен быть NEW.
    // - Если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
    // - Во всех остальных случаях статус должен быть IN_PROGRESS.
    private void calcEpicStatus(List<Subtask> subtasksOfEpic) {
        boolean allNew = subtasksOfEpic.stream()
                .allMatch(subTask -> subTask.getStatus() == TaskStatus.NEW);
        boolean allDone = subtasksOfEpic.stream()
                .allMatch(subTask -> subTask.getStatus() == TaskStatus.DONE);

        if (allNew) {
            status = TaskStatus.NEW;
        } else if (allDone) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    // Обновляем продолжительность задачи, время начала и окончания
    // Продолжительность эпика — сумма продолжительностей всех его подзадач.
    // Время начала — дата старта самой ранней подзадачи, а время завершения — время окончания самой поздней из задач
    private void calcEpicTimes(List<Subtask> subtasksOfEpic) {
        if (subtasksOfEpic.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }

        // Продолжительность эпика — сумма продолжительностей всех его подзадач
        duration = subtasksOfEpic.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        // Время начала — дата старта самой ранней подзадачи
        startTime = subtasksOfEpic.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Время завершения — время окончания самой поздней из задач
        endTime = subtasksOfEpic.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        // Для эпика в строковом представлении не фиксируем текущие значения таймингов,
        // поскольку они высчитываются на основе его подзадач
        return String.format("%d,%s,%s,%s,%s,,0,",
                id, type, title, status, description);
    }
}
