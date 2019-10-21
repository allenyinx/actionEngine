package com.airta.action.engine.message.report;

import com.airta.action.engine.entity.report.Element;
import com.airta.action.engine.parser.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                List elementList = new ArrayList(){};
                elementList.add(rootElement);
                elementToJsonFile(elementList);
            }
        }

    }

    public static void main(String[] args) {

        ReportSubscriber reportSubscriber = new ReportSubscriber();
        List<Element> rootList = new ArrayList<>();
        Element element = new Element();
        element.setText("single_01");
        element.setElementId("single_01");
        List<Element> children = new ArrayList<>();
        Element element_01 = new Element();
        element_01.setText("single_01");
        Element element_02 = new Element();
        element_02.setText("single_02");
        Element element_03 = new Element();
        element_03.setText("single_03");
        Element element_04 = new Element();
        element_04.setText("single_04");
        children.add(element_01);
        children.add(element_02);
        children.add(element_03);
        children.add(element_04);
        element.setChildren(children);
        rootList.add(element);
        reportSubscriber.elementToJsonFile(rootList);
    }

    private void elementToJsonFile(List rootElement) {

        File report = new File("/Users//Documents/IdeaProjects/actionEngine/sitemap/root.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(report, rootElement);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
