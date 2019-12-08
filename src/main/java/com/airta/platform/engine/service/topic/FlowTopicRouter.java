package com.airta.platform.engine.service.topic;

import com.airta.platform.engine.parser.JsonParser;
import com.airta.platform.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowTopicRouter implements ITopicRouter {

    @Autowired
    private JsonParser jsonParser;

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {
        logger.info("## on flow topic router ..");
        return false;
    }
}
