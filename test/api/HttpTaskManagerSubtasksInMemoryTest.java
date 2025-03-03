package api;

import managers.InMemoryTaskManager;

class HttpTaskManagerSubtasksInMemoryTest extends HttpTaskManagerSubtasksTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}