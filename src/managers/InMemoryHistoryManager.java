package managers;

import entities.Node;
import entities.Task;
import interfaces.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> historyHead;
    private Node<Task> historyTail;
    private int tasksSize = 0;
    private final Map<Integer, Node<Task>> idsToTasks = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        int taskId = task.getId();

        if (idsToTasks.containsKey(taskId)) {
            removeNode(idsToTasks.get(taskId));
        }
        idsToTasks.put(taskId, linkLast(task));
    }

    @Override
    public void remove(int id) {
        if (!idsToTasks.containsKey(id)) {
            return;
        }
        removeNode(idsToTasks.get(id));
        idsToTasks.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    // Добавляет в связный список новую задачу
    private Node<Task> linkLast(Task task) {
        if (task == null) {
            return null;
        }

        Node<Task> oldTail = historyTail;
        Node<Task> newNode = new Node<>(oldTail, task, null);
        historyTail = newNode;

        if (oldTail == null) {
            historyHead = newNode;
        } else {
            oldTail.next = newNode;
        }
        tasksSize++;

        idsToTasks.put(task.getId(), newNode);
        return newNode;
    }

    // Удаляет ноду из связного списка
    private void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }
        Node<Task> prevNode = node.prev;
        Node<Task> nextNode = node.next;

        if (prevNode != null) {
            prevNode.next = nextNode;
        } else {
            historyHead = nextNode;
        }

        if (nextNode != null) {
            nextNode.prev = prevNode;
        } else {
            historyTail = prevNode;
        }

        node.data = null;
        node.prev = null;
        node.next = null;

        tasksSize--;
    }

    // Возвращает историю просмотра задач в виде обычного списка
    private List<Task> getTasks() {
        if (tasksSize == 0) {
            return new ArrayList<>();
        }
        List<Task> tasksList = new ArrayList<>(tasksSize);

        Node<Task> currentNode = new Node<>(historyHead);
        tasksList.add(currentNode.data);

        while (currentNode.next != null) {
            currentNode = new Node<>(currentNode.next);
            tasksList.add(currentNode.data);
        }
        return tasksList;
    }
}
