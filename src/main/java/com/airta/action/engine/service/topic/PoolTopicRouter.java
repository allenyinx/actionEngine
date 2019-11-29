package com.airta.action.engine.service.topic;

import com.airta.action.engine.entity.pool.AgentPool;
import com.airta.action.engine.entity.report.Element;
import com.airta.action.engine.k8s.PodTaskProcessor;
import com.airta.action.engine.parser.JsonParser;
import com.airta.action.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

                if(agentPool.getType().equals("init")) {
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
}
