package interfaces;

import entities.Task;

import java.util.List;

public interface HistoryManager {
    // Добавляет задачу в историю
    void add(Task task);

    // Удаляет задачу из истории
    void remove(int id);

    // Получает историю просмотра задач
    List<Task> getHistory();
}
