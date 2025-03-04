package api;

import managers.InMemoryTaskManager;

class HttpTaskManagerPrioritizedInMemoryTest extends HttpTaskManagerPrioritizedTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}
