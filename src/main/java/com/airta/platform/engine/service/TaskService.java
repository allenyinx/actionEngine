package com.airta.platform.engine.service;

import com.airta.platform.engine.runtime.Task;
import com.airta.platform.engine.runtime.TaskManager;
import com.airta.platform.engine.runtime.impl.KubeTaskAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TaskService {

    private TaskManager taskManager = null;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void initTaskManager(Task task) {

        String rootUrl = task.getRootUrl();
        String featureCondition = task.getPrepScript();
        Map<String, String> cxtInfo = task.getCxtInfo();
        try {
            taskManager = new TaskManager(rootUrl, featureCondition, cxtInfo);
            logger.info("## task {} init successfully.", taskManager.toString());
            addAgents(task.getThreads());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }

    }

    public void runTask() {

        if(taskManager!=null) {
            try {
                taskManager.runBlocking();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    private void addAgents(int threads) {

        if(taskManager!=null && threads > 0) {
            while(threads-->0) {
                KubeTaskAgent kubeTaskAgent = new KubeTaskAgent();
                taskManager.addTaskAgent(kubeTaskAgent);
            }

        }
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
}
