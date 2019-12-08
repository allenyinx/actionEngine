package com.airta.platform.engine.message.flow;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * @author allenyin
 */
public class FlowSubscriber {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

//    @KafkaListener(topics = {"flow"})
    public void listen(ConsumerRecord<?, ?> record) {
        logger.info("listen flow topic: " + record.key());
        logger.info("listen flow topic value: " + record.value().toString());
    }
}
