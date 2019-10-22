package com.airta.action.engine.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;


/**
 * @author allenyin
 */
@RestController
@RequestMapping("/api")
public class EngineDefaultController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate kafkaTemplate;

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

    @GetMapping(value = "/version")
    public Object checkVersion() {

        return "phase1_1.0.0";
    }

}
