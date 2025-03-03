package api;

import managers.InMemoryTaskManager;

class HttpTaskManagerEpicsInMemoryTest extends HttpTaskManagerEpicsTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}
