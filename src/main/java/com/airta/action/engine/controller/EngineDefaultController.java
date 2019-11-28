package com.airta.action.engine.controller;


import com.airta.action.engine.service.topic.PoolTopicRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
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
    public Object initAgent() {

        logger.info("init request received.");

        poolTopicRouter.actionOnTopic("","");

        return 200;
    }

    @GetMapping(value = "/version")
    public Object checkVersion() {

        String currentTimestamp = new SimpleDateFormat("yyyyMMddHHmm").format( new Date() );
        return "phase2_1."+currentTimestamp;
    }

}
