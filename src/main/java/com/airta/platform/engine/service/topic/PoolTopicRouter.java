package com.airta.platform.engine.service.topic;

import com.airta.platform.engine.entity.pool.AgentPool;
import com.airta.platform.engine.entity.pool.PodSessionPool;
import com.airta.platform.engine.k8s.PodTaskProcessor;
import com.airta.platform.engine.parser.JsonParser;
import com.airta.platform.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author allenyin
 */
@Component
public class PoolTopicRouter implements ITopicRouter {

    @Autowired
    private PodTaskProcessor podTaskProcessor;

    @Autowired
    private JsonParser jsonParser;

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {
        logger.info("## on pool init topic router ..");

        if (incomingValueObj != null) {
            Object agentPoolObj = jsonParser.resolveIncomingMessage(incomingValueObj.toString(), AgentPool.class);
            if (agentPoolObj != null) {
                AgentPool agentPool = (AgentPool) agentPoolObj;
                logger.info("## now we resolved agentPool: {}", agentPool);
                agentPool.setPoolName(incomingKeyObj.toString());

                if("init".equals(agentPool.getType())) {
                    return podTaskProcessor.scheduleInitAgent(agentPool);
                } else {
                    return podTaskProcessor.scheduleCleanAgents(agentPool);
                }
            }
            return true;
        } else {
            logger.error("## invalid incoming message for pool scheduler ..");
            return false;
        }
    }

    public PodSessionPool getPodSessionPool(String poolName) {

        PodSessionPool podSessionPool = podTaskProcessor.readPodSessionPool(poolName);
        if(podSessionPool!=null) {
            return podSessionPool;
        } else {
            return new PodSessionPool();
        }

    }
}