package com.airta.platform.engine.runtime.impl;

import com.airta.platform.engine.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author allenyin
 */
public class KubeTaskAgent implements TaskAgent {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean run(Task task) throws Exception {

        logger.info("## run task: {}", task.getId());
        // produce action message.
        String actionScriptContent = task.getScript();
        Map<String, String> taskCxtInfoMap = task.getCxtInfo();
        String preScriptContent = task.getPrepScript();

        ActionScript actionScript = new ActionScript(actionScriptContent, taskCxtInfoMap);
        ActionScript preActionScript = new ActionScript(preScriptContent, taskCxtInfoMap);

        prepareAgent(preActionScript, task);
        runAction(actionScript, task);

        return false;
    }

    private void prepareAgent(ActionScript preActionScript, Task task) {

        logger.info("## prepare agent for task: {} .", task.getId());
    }

    private void runAction(ActionScript actionScript, Task task) {

        logger.info("## run action: {} for task: {}", actionScript.toString(), task.getId());
    }

    @Override
    public TaskResult update(String taskId) {
        //produce update sitemap message
        logger.info("## update task: {}", taskId);
        return new TaskResult(TaskStatus.SUCCESS, taskId, 0, 0);
    }

    @Override
    public TaskResult cancel(String taskId) {
        logger.info("## cancel task: {}", taskId);
        return new TaskResult(TaskStatus.CANCELED, taskId, 0, 0);
    }

    @Override
    public TaskData[] getData(String taskId, String filter, int types) {
        return new TaskData[0];
    }

    @Override
    public int getCapacity() {
        return 0;
    }
}
