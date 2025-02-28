package base;

import managers.InMemoryTaskManager;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    public InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}