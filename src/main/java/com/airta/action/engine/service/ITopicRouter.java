package com.airta.action.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public interface ITopicRouter {

    Logger logger = LoggerFactory.getLogger("TopicRouter");

    boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj);
}
