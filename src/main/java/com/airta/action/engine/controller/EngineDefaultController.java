package com.airta.action.engine.controller;


import com.airta.action.engine.entity.pool.AgentPool;
import com.airta.action.engine.parser.JsonParser;
import com.airta.action.engine.service.topic.PoolTopicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author allenyin
 */
@RestController
@RequestMapping("/api")
public class EngineDefaultController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private PoolTopicRouter poolTopicRouter;

    @Autowired
    private JsonParser jsonParser;

    @GetMapping(value = "/alive")
    public HttpStatus checkMessageStatus() {
        try {
            String message = "test for flow message";
            logger.info("kafka message ={}", message);
            kafkaTemplate.send("flow", "key", message);
            logger.info("sending to kafka successfully");
            return HttpStatus.OK;
        } catch (Exception e) {
            logger.error("sending to kafka fail", e);
        }
        return HttpStatus.BAD_REQUEST;
    }

    @PostMapping(value = "/init", produces = "application/json")
    @ResponseBody
    public Object initAgent(Object agentPoolMessage) {

        logger.info("init request received.");

        poolTopicRouter.actionOnTopic("general", agentPoolMessage);

        return 200;
    }

    @GetMapping(value = "/sitemap", produces = "application/json")
    public org.json.simple.JSONArray getSiteMapJSON() {

        return jsonParser.readFronJSONFile();
    }

    @GetMapping(value = "/pool", produces = "application/json")
    public org.json.simple.JSONArray getAgentPool() {

        return jsonParser.readFronJSONFile();
    }

    @GetMapping(value = "/version")
    public Object checkVersion() {

        String currentTimestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        return "phase2_1." + currentTimestamp;
    }

}
