package com.airta.platform.engine.message.action;

import com.airta.platform.engine.entity.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author allenyin
 */
@Service
public class ActionProducer {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${kafka.producer.topic}")
    private String actionTopic;

    public void addAction(Action action) {

        createActionMessage(actionTopic, "", "");
    }

    /**
     * add single action message into ActionPool.
     */
    public void createActionMessage(String topic, String key, String message) {

        try {
            logger.info("kafka message ={}", message);
            kafkaTemplate.send(topic, key, message);
            logger.info("sending to kafka successfully");
        } catch (Exception e) {
            logger.error("sending to kafka fail", e);
        }
    }
}
