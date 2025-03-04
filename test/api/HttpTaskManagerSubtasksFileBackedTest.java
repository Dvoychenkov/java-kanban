package api;

import managers.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;

class HttpTaskManagerSubtasksFileBackedTest extends HttpTaskManagerSubtasksTest<FileBackedTaskManager> {
    @Override
    protected FileBackedTaskManager createTaskManager() {
        File tempFile;
        try {
            tempFile = File.createTempFile("test_task_manager_data", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tempFile.deleteOnExit();
        return FileBackedTaskManager.loadFromFile(tempFile);
    }
}