package com.airta.action.engine.service.topic;

import com.airta.action.engine.parser.JsonParser;
import com.airta.action.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PoolTopicRouter implements ITopicRouter {

    @Autowired
    private JsonParser jsonParser;

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {
        return false;
    }
}
