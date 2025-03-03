package api;

import managers.InMemoryTaskManager;

class HttpTaskManagerTasksInMemoryTest extends HttpTaskManagerTasksTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}