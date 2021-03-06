package kidnox.eventbus.test.simple;

import kidnox.eventbus.Task;
import kidnox.eventbus.Execute;

@Task
public class SimpleTask {

    volatile int executeCount;

    @Execute public void execute() {
        executeCount++;
    }

    public int getExecuteCount() {
        return executeCount;
    }
}
