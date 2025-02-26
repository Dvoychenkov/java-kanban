package entities;

import enums.TaskStatus;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
        this.subtasksIds = epic.subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, List<Integer> subtasksIds) {
        this(title, description, status);
        this.subtasksIds = subtasksIds;
    }

    public Epic(String title, String description, TaskStatus status, int[] subtasksIds) {
        this(title, description, status);

        Arrays.stream(subtasksIds)
            .forEach(subTasksId -> this.subtasksIds.add(subTasksId));
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

    // Продолжительность эпика — сумма продолжительностей всех его подзадач.
    // Время начала — дата старта самой ранней подзадачи, а время завершения — время окончания самой поздней из задач
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
            setStatus(TaskStatus.NEW);
            setSubtasksIds(new ArrayList<>());
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
        TaskStatus statusToBeSet = TaskStatus.IN_PROGRESS;
        boolean hasInProgress = false;
        boolean hasNew = false;
        boolean hasDone = false;
        for (Task subTask : subtasksOfEpic) {
            switch (subTask.getStatus()) {
                case NEW:
                    hasNew = true;
                    break;
                case IN_PROGRESS:
                    hasInProgress = true;
                    break;
                case DONE:
                    hasDone = true;
                    break;
            }

            if (hasInProgress || (hasNew && hasDone)) {
                status = statusToBeSet;
                return;
            }
        }

        if (hasDone) {
            statusToBeSet = TaskStatus.DONE;
        } else if (hasNew) {
            statusToBeSet = TaskStatus.NEW;
        }
        status = statusToBeSet;
    }

    // Обновляем продолжительность задачи, время начала и окончания
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

}
