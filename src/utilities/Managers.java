package utilities;

import interfaces.*;
import managers.*;

public class Managers {
    private static final int IN_MEMORY_HISTORY_MAX_CAPACITY = 10;

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(IN_MEMORY_HISTORY_MAX_CAPACITY);
    }

}
