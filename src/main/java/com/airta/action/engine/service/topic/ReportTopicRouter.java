package com.airta.action.engine.service.topic;

import com.airta.action.engine.entity.report.Element;
import com.airta.action.engine.parser.JsonParser;
import com.airta.action.engine.service.ITopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportTopicRouter implements ITopicRouter {

    @Autowired
    private JsonParser jsonParser;

    @Override
    public boolean actionOnTopic(Object incomingKeyObj, Object incomingValueObj) {
        logger.info("## on report topic processing ..");

        if (incomingValueObj != null) {
            Object rootElementObject = jsonParser.resolveIncomingMessage(incomingValueObj.toString(), Element.class);
            if (rootElementObject != null) {
                Element rootElement = (Element) rootElementObject;
                logger.info("## now we resolved report element: {}", rootElement);
                jsonParser.updateExistingElementWithNewMessage(rootElement);
            }
            return true;
        } else {
            return false;
        }
    }
}
