package com.airta.platform.engine.runtime.impl;

import com.airta.platform.engine.entity.pool.PodSession;
import com.airta.platform.engine.nanoscript.Oper;
import com.airta.platform.engine.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author allenyin
 */
public class KubeTaskAgent implements TaskAgent {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PodSession podSession;
    private String agentName;

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

    /**
     * do preparation operations on the live or initialized agent:
     * e.g. run to a expected context or page.
     *
     * @param preActionScript
     * @param task
     */
    private void prepareAgent(ActionScript preActionScript, Task task) {

        logger.info("## prepare agent for task: {} .", task.getId());
    }

    private void runAction(ActionScript actionScript, Task task) {

        logger.info("## run action: {} for task: {}", actionScript.toString(), task.getId());

        Oper nextOper = new Oper("", false, null, "");
        Oper oper = new Oper("", false, nextOper, "");
        Oper outOper = new Oper("", false, null, "");
        List paras = Collections.emptyList();
        paras.add(oper);

        String agentAPIAddress = readRemoteAgentSessionInfo();
        RestTemplate restTemplate = new RestTemplate();
        actionScript.setRestTemplate(restTemplate);

        actionScript.processAPI("", "click", paras, outOper, agentAPIAddress);
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

    private String readRemoteAgentSessionInfo() {

        String serviceName = podSession.getService();
        int servicePort = podSession.getPort();

        return "http://"+serviceName+":"+servicePort+"/api/run";
    }

    public PodSession getPodSession() {
        return podSession;
    }

    public void setPodSession(PodSession podSession) {
        this.podSession = podSession;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}
