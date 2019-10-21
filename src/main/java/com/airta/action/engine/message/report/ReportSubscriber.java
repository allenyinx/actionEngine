package com.airta.action.engine.message.report;

import com.airta.action.engine.entity.report.Element;
import com.airta.action.engine.parser.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * @author allenyin
 */
public class ReportSubscriber {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JsonParser jsonParser = new JsonParser();

    @KafkaListener(topics = {"report"})
    public void listen(ConsumerRecord<?, ?> record) {
        logger.info("listen report topic: " + record.key());
        logger.info("listen report topic value: " + record.value().toString());

        if(record.value()!=null) {
            Object rootElementObject = jsonParser.resolveIncomingMessage(record.value().toString(), Element.class);
            if(rootElementObject!=null) {
                Element rootElement  = (Element)rootElementObject;
                logger.info("## now we resolved report element: {}", rootElement);
            }
        }

    }
}
