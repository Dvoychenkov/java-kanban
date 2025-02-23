package utilities;

import interfaces.*;
import managers.*;

import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return FileBackedTaskManager.loadFromFile(new File("task_manager_data.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
