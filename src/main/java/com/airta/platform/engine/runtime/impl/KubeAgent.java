package com.airta.platform.engine.runtime.impl;

import com.airta.platform.engine.runtime.Task;
import com.airta.platform.engine.runtime.TaskAgent;
import com.airta.platform.engine.runtime.TaskData;
import com.airta.platform.engine.runtime.TaskResult;

import java.util.Map;

public class KubeAgent implements TaskAgent {


    @Override
    public boolean run(Task task) throws Exception {
        // produce action message.
        String actionScriptContent = task.getScript();
        Map<String, String> taskCxtInfoMap = task.getCxtInfo();
        String preScriptContent = task.getPrepScript();

        ActionScript actionScript = new ActionScript(actionScriptContent, taskCxtInfoMap);
        ActionScript preActionScript = new ActionScript(preScriptContent, taskCxtInfoMap);
        
        prepareAgent(preActionScript);
        runAction(actionScript);

        return false;
    }

    private void prepareAgent(ActionScript preActionScript) {


    }

    private void runAction(ActionScript actionScript) {

    }

    @Override
    public TaskResult update(String taskId) {
        //produce update sitemap message
        return null;
    }

    @Override
    public TaskResult cancel(String taskId) {
        return null;
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
