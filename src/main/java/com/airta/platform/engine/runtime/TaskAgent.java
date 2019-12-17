package com.airta.platform.engine.runtime;

public interface TaskAgent {

    boolean run(Task task) throws Exception;

    TaskResult update(String taskId);

    TaskResult cancel(String taskId);

    TaskData[] getData(String taskId, String filter, int types);

    int getCapacity();
}
