package managers;

import entities.Task;
import interfaces.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final int historyMaxCapacity;
    private final List<Task> tasksHistory = new ArrayList<>();

    public InMemoryHistoryManager(int historyMaxCapacity) {
        this.historyMaxCapacity = historyMaxCapacity;
    }

    @Override
    public List<Task> getHistory() {
        return tasksHistory;
    }

    @Override
    public void add(Task task) {
        int tasksHistorySize = tasksHistory.size();

        // Размер списка для хранения просмотров не должен превышать заранее определённый лимит
        if (tasksHistorySize >= historyMaxCapacity) {
            while (tasksHistory.size() >= historyMaxCapacity) {
                tasksHistory.removeFirst();
            }
        }

        tasksHistory.add(task);
    }
}
