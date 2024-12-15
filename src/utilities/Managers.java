package utilities;

import interfaces.TaskManager;
import managers.*;

public class Managers {
    private static final int HISTORY_MAX_CAPACITY = 10;

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(HISTORY_MAX_CAPACITY);
    }

}
