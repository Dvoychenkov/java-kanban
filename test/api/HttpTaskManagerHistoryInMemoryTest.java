package api;

import managers.InMemoryTaskManager;

class HttpTaskManagerHistoryInMemoryTest extends HttpTaskManagerHistoryTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}
