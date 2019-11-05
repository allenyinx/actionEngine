package com.airta.action.engine.message;

import com.airta.action.engine.service.*;
import com.airta.action.engine.service.topic.FlowTopicRouter;
import com.airta.action.engine.service.topic.PoolTopicRouter;
import com.airta.action.engine.service.topic.ReportTopicRouter;
import com.airta.action.engine.service.topic.UnknownTopicRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageActionFactory {

    private final ReportTopicRouter reportTopicRouter;
    private final PoolTopicRouter poolTopicRouter;
    private final FlowTopicRouter flowTopicRouter;
    private final UnknownTopicRouter unknownTopicRouter;

    @Autowired
    private MessageActionFactory(ReportTopicRouter reportTopicRouter, PoolTopicRouter poolTopicRouter,
                                 FlowTopicRouter flowTopicRouter, UnknownTopicRouter unknownTopicRouter) {

        this.reportTopicRouter = reportTopicRouter;
        this.poolTopicRouter = poolTopicRouter;
        this.flowTopicRouter = flowTopicRouter;
        this.unknownTopicRouter = unknownTopicRouter;
    }

    public ITopicRouter builder(String topicName) {

        switch (topicName) {
            case "report":
                return reportTopicRouter;
            case "flow":
                return flowTopicRouter;
            case "pool":
                return poolTopicRouter;
            default:
                return unknownTopicRouter;
        }
    }
}
