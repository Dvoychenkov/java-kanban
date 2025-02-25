package managers;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import enums.TaskStatus;
import enums.TaskType;
import exceptions.ManagerSaveException;
import exceptions.ManagerLoadException;
import interfaces.TaskManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File tasksStorage;

    public FileBackedTaskManager(File tasksStorage) {
        super();
        this.tasksStorage = tasksStorage;
    }

    public static FileBackedTaskManager loadFromFile(File tasksStorage) {
        FileBackedTaskManager manager = new FileBackedTaskManager(tasksStorage);
        manager.loadFromFile();
        return manager;
    }

    private void loadFromFile() {
        try {
            String fileContent = Files.readString(tasksStorage.toPath());

            if (fileContent.isBlank()) {
                return;
            }
            String[] lines = fileContent.split("\n");
            if (lines.length < 2) { // Файл содержит только заголовок
                return;
            }

            // Убираем строку-заголовок и сортируем строки по возрастанию для сохранения порядка id задач
            String[] filteredAndSortedLines = Arrays.copyOfRange(lines, 1, lines.length);
            Arrays.sort(filteredAndSortedLines);

            for (String filteredAndSortedLine : filteredAndSortedLines) {
                Task task = fromString(filteredAndSortedLine);
                switch (task.getType()) {
                    case TASK:
                        addNewTask(task);
                        break;
                    case SUBTASK:
                        addNewSubtask((Subtask) task);
                        break;
                    case EPIC:
                        addNewEpic((Epic) task);
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException(String.format("Ошибка при загрузке задач из файла '%s': %s",
                    tasksStorage.getAbsolutePath(), e.getMessage()));
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tasksStorage))) {
            writer.write("id,type,name,status,description,epic\n"); // Заголовок CSV

            for (Task task : getTasks()) {
                writer.write(task + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(subtask + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(epic + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException(String.format("Ошибка при сохранении задачи в файл '%s': %s",
                    tasksStorage.getAbsolutePath(), e.getMessage()));
        }
    }

    private static Task fromString(String line) {
        String[] fields = line.trim().split(",");
        if (fields.length < 5) {
            throw new ManagerLoadException(String.format("Некорректная длина строки: %s", line));
        }

        int id;
        try {
            id = Integer.parseInt(fields[0]);
        } catch (NumberFormatException ex) {
            throw new ManagerLoadException(String.format("Некорректный id строки: %s", line));
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
                        throw new ManagerLoadException(String.format("Некорректный epic id строки: %s", line));
                    }
                    subtask.setEpicId(epicId);
                }

                return subtask;
            default:
                throw new ManagerLoadException(String.format("Неизвестный тип задачи: %s", type));
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    public static void main(String[] args) throws IOException {
        // 1. Если файл существует - удаляем и создаём новый пустой
        File storageFile = new File("task_manager_data.csv");
        Path pathToFile = storageFile.toPath();
        if (Files.exists(pathToFile)) {
            Files.delete(pathToFile);
        }
        Files.createFile(pathToFile);

        TaskManager taskManagerFirstInit = FileBackedTaskManager.loadFromFile(storageFile);

        // 1. Создаём задачи, эпики и подзадачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW);
        int task1Id = taskManagerFirstInit.addNewTask(task1);
        int task2Id = taskManagerFirstInit.addNewTask(task2);

        Epic epic1 = new Epic("Epic 1", "Epic with subtasks", TaskStatus.NEW);
        int epic1Id = taskManagerFirstInit.addNewEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask 1", "Subtask 1 description", TaskStatus.NEW, epic1Id);
        int subtask1Id = taskManagerFirstInit.addNewSubtask(subtask1);

        Subtask subtask2 = new Subtask("Subtask 2", "Subtask 2 description", TaskStatus.NEW, epic1Id);
        int subtask2Id = taskManagerFirstInit.addNewSubtask(subtask2);

        Subtask subtask3 = new Subtask("Subtask 3", "Subtask 3 description", TaskStatus.NEW, epic1Id);
        int subtask3Id = taskManagerFirstInit.addNewSubtask(subtask3);

        Epic epic2 = new Epic("Epic 2", "Epic without subtasks", TaskStatus.NEW);
        int epic2Id = taskManagerFirstInit.addNewEpic(epic2);

        // 2. Снова создаём менеджер из того же файла
        TaskManager loadedManagerSecondInit = FileBackedTaskManager.loadFromFile(storageFile);

        // 3. Сверяем
        System.out.println("=".repeat(50));
        System.out.println("[taskManagerFirstInit]:");
        System.out.println(taskManagerFirstInit.getTasks());
        System.out.println(taskManagerFirstInit.getSubtasks());
        System.out.println(taskManagerFirstInit.getEpics());
        System.out.println("=".repeat(50));

        System.out.println("=".repeat(50));
        System.out.println("[loadedManagerSecondInit]:");
        System.out.println(loadedManagerSecondInit.getTasks());
        System.out.println(loadedManagerSecondInit.getSubtasks());
        System.out.println(loadedManagerSecondInit.getEpics());
        System.out.println("=".repeat(50));

        System.out.printf("Tasks equality: %b%n", taskManagerFirstInit.getTasks().equals(loadedManagerSecondInit.getTasks()));
        System.out.printf("Subtasks equality: %b%n", taskManagerFirstInit.getSubtasks().equals(loadedManagerSecondInit.getSubtasks()));
        System.out.printf("Epics equality: %b%n", taskManagerFirstInit.getEpics().equals(loadedManagerSecondInit.getEpics()));
        System.out.println("=".repeat(50));
    }
}
