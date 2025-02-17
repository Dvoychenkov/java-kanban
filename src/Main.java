import entities.Epic;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import interfaces.TaskManager;
import utilities.Managers;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // 1. Создаём две обычные задачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        // 1. Создаём эпик с тремя подзадачами
        Epic epic1 = new Epic("Epic 1", "Epic with subtasks", TaskStatus.NEW);
        int epic1Id = taskManager.addNewEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 description", TaskStatus.NEW);
        subtask1.setEpicId(epic1Id);
        int subtask1Id = taskManager.addNewSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 description", TaskStatus.NEW);
        subtask2.setEpicId(epic1Id);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask 3", "Subtask 3 description", TaskStatus.NEW);
        subtask3.setEpicId(epic1Id);
        int subtask3Id = taskManager.addNewSubtask(subtask3);

        // 1. Создаём эпик без подзадач
        Epic epic2 = new Epic("Epic 2", "Epic without subtasks", TaskStatus.NEW);
        int epic2Id = taskManager.addNewEpic(epic2);

        // 2. Запрашиваем задачи в разном порядке
        taskManager.getTask(task1Id);
        taskManager.getEpic(epic1Id);
        taskManager.getTask(task2Id);
        taskManager.getEpic(epic2Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getSubtask(subtask2Id);
        taskManager.getSubtask(subtask3Id);

        // 3. Выводим историю и убеждаемся, что нет повторов
        System.out.println("=".repeat(10));
        System.out.println("История запросов - до удалений:");
        printHistory(taskManager);

        // 4. Удаляем одну из задач и проверяем, что её нет в истории
        taskManager.deleteTaskById(task1Id);
        System.out.println("=".repeat(10));
        System.out.println("История запросов - после удаления Task 1:");
        printHistory(taskManager);

        // 5. Удаляем эпик с тремя подзадачами и проверяем, что он и подзадачи исчезли из истории
        taskManager.deleteEpicById(epic1Id);
        System.out.println("=".repeat(10));
        System.out.println("История запросов - После удаления Epic 1 (и его подзадач):");
        printHistory(taskManager);
    }

    private static void printHistory(TaskManager taskManager) {
        List<Task> history = taskManager.getHistory();
        for (Task task : history) {
            System.out.println(task);
        }
    }
}

