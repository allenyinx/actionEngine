package com.airta.platform.engine.service;

import com.airta.platform.engine.entity.pool.AgentPool;
import com.airta.platform.engine.entity.pool.PodSession;
import com.airta.platform.engine.entity.pool.PodSessionPool;
import com.airta.platform.engine.k8s.PodTaskProcessor;
import com.airta.platform.engine.message.action.ActionProducer;
import com.airta.platform.engine.runtime.InitTask;
import com.airta.platform.engine.runtime.Task;
import com.airta.platform.engine.runtime.TaskManager;
import com.airta.platform.engine.runtime.impl.KubeTaskAgent;
import com.airta.platform.engine.service.topic.PoolTopicRouter;
import io.kubernetes.client.models.V1Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    private final PodTaskProcessor podTaskProcessor;

    private final RestService restService;

    private final ActionProducer actionProducer;

    @Autowired
    public TaskService(PoolTopicRouter poolTopicRouter, PodTaskProcessor podTaskProcessor, RestService restService, ActionProducer actionProducer) {

        this.poolTopicRouter = poolTopicRouter;
        this.podTaskProcessor = podTaskProcessor;
        this.restService = restService;
        this.actionProducer = actionProducer;
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

    public void initTask(InitTask initTask) {

        AgentPool agentPool = new AgentPool();
        agentPool.setPoolName(initTask.getSiteName());
        agentPool.setAgentSize(initTask.getAgentNumber());
        agentPool.setPoolGroup(1);
        agentPool.setUrl(initTask.getSiteUrl());

        List<V1Service> agentServiceList = createKubeAgent(agentPool);
        if (!agentServiceList.isEmpty()) {
            V1Service agentService = agentServiceList.get(0);

            String crawlerServiceUrl = agentService.getMetadata().getName()+".airgent";

            if (restService.waitForServiceAvail(crawlerServiceUrl)) {

                restService.postEntity("http://"+crawlerServiceUrl + ":8228/api/initSiteMap", new HttpEntity<>(Collections.emptyMap()), Object.class);
            }
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

    private List<V1Service> createKubeAgent(AgentPool agentPool) {

        return podTaskProcessor.scheduleInitAgent(agentPool);
    }

    private KubeTaskAgent buildKubeAgent(Task task) {

        PodSessionPool podSessionPool = poolTopicRouter.getPodSessionPool(task.getId());
        if (podSessionPool == null || podSessionPool.getPoolName() == null) {
            return new KubeTaskAgent();
        } else {
            List<PodSession> podSessionList = podSessionPool.getPodSessionList();
            PodSession targetPodSession = findNearestAgentFromPodSessions(task, podSessionList);
            KubeTaskAgent kubeTaskAgent = new KubeTaskAgent();
            kubeTaskAgent.setPodSession(targetPodSession);
            kubeTaskAgent.setAgentName(task.getId() + "_" + targetPodSession.getName());
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
