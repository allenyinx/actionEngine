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
@RequestMapping("/engine")
public class EngineDefaultController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @GetMapping(value = "/alive")
    public HttpStatus checkMessageStatus(HttpServletRequest request) {
        try {
            String message = request.getParameter("message");
            logger.info("kafka message ={}", message);
            kafkaTemplate.send("test", "key", message);
            logger.info("sending to kafka successfully");
            return HttpStatus.ACCEPTED;
        } catch (Exception e) {
            logger.error("sending to kafka fail", e);
        }
        return HttpStatus.BAD_REQUEST;
    }

    @GetMapping(value = "/version")
    public Object checkVersion() {

        return "v0.0.1";
    }

}
