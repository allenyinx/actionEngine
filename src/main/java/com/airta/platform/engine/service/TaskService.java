package com.airta.platform.engine.service;

import com.airta.platform.engine.entity.pool.PodSession;
import com.airta.platform.engine.entity.pool.PodSessionPool;
import com.airta.platform.engine.runtime.Task;
import com.airta.platform.engine.runtime.TaskManager;
import com.airta.platform.engine.runtime.impl.KubeTaskAgent;
import com.airta.platform.engine.service.topic.PoolTopicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author allenyin
 */
@Service
public class TaskService {

    private TaskManager taskManager = null;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PoolTopicRouter poolTopicRouter;

    @Autowired
    public TaskService(PoolTopicRouter poolTopicRouter) {

        this.poolTopicRouter = poolTopicRouter;
    }

    public void initTaskManager(Task task) {

        String rootUrl = task.getRootUrl();
        String featureCondition = task.getPrepScript();
        Map<String, String> cxtInfo = task.getCxtInfo();
        try {
            taskManager = new TaskManager(rootUrl, featureCondition, cxtInfo);
            logger.info("## task {} init successfully.", taskManager.toString());
            addAgents(task);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }

    }

    public void runTask() {

        if (taskManager != null) {
            try {
                taskManager.runBlocking();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    private void addAgents(Task task) {

        int taskThread = task.getThreads();
        if (taskManager != null && taskThread > 0) {
            while (taskThread-- > 0) {
                taskManager.addTaskAgent(buildKubeAgent(task));
            }
        }
    }

    private KubeTaskAgent buildKubeAgent(Task task) {

        PodSessionPool podSessionPool = poolTopicRouter.getPodSessionPool(task.getId());
        if(podSessionPool==null || podSessionPool.getPoolName()==null) {
            return new KubeTaskAgent();
        } else {
            List<PodSession> podSessionList = podSessionPool.getPodSessionList();
            PodSession targetPodSession = findNearestAgentFromPodSessions(task, podSessionList);
            KubeTaskAgent kubeTaskAgent = new KubeTaskAgent();
            kubeTaskAgent.setPodSession(targetPodSession);
            kubeTaskAgent.setAgentName(task.getId()+"_"+targetPodSession.getName());
            return kubeTaskAgent;
        }
    }

    private PodSession findNearestAgentFromPodSessions(Task task, List<PodSession> podSessionList) {

        return podSessionList.get(0);
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
}
