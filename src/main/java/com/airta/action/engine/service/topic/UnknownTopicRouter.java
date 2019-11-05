package com.airta.action.engine.service.topic;

import com.airta.action.engine.service.ITopicRouter;
import org.springframework.stereotype.Component;

@Component
public class UnknownTopicRouter implements ITopicRouter {

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {

        logger.info("## on unknown topic router ..");
        return false;
    }
}
