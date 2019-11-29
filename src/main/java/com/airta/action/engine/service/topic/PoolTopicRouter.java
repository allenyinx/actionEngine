package com.airta.action.engine.service.topic;

import com.airta.action.engine.k8s.PodTaskProcessor;
import com.airta.action.engine.parser.JsonParser;
import com.airta.action.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PoolTopicRouter implements ITopicRouter {

    @Autowired
    private PodTaskProcessor podTaskProcessor;

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {
        logger.info("## on pool init topic router ..");


        return podTaskProcessor.scheduleInitAgent();
    }
}
