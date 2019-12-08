package com.airta.platform.engine.message;

import com.airta.platform.engine.entity.report.Element;
import com.airta.platform.engine.parser.JsonParser;
import com.airta.platform.engine.service.ITopicRouter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author allenyin
 */
@Service
public class ActionSubscriber {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private MessageActionFactory messageActionFactory;

    @KafkaListener(topics = {"report", "pool", "flow"})
    public void listen(ConsumerRecord<?, ?> record) {
        String incomingTopicName = record.topic();
        Object incomingKeyObj = record.key();
        Object incomingValueObj = record.value();

        logger.info("Received topic: {}, key: {} ", incomingTopicName, incomingKeyObj);
        logger.info("topic value: {} ", incomingValueObj);

        ITopicRouter topicRouter = messageActionFactory.builder(incomingTopicName);
        topicRouter.actionOnTopic(incomingKeyObj, incomingValueObj);
    }

    public static void main(String[] args) {

        ActionSubscriber actionSubscriber = new ActionSubscriber();
//        List<Element> elementList = reportSubscriber.initMap();
//        reportSubscriber.writeToFile(elementList);

        Element incomingElement = actionSubscriber.updateMap();
        actionSubscriber.updateExs(incomingElement);
    }

    private void updateExs(Element incomingElement) {
        jsonParser.updateExistingElementWithNewMessage(incomingElement);
    }

    public List<Element> initMap() {

        List<Element> rootList = new ArrayList<>();
        Element element = new Element();
        element.setText("single_01");
        element.setElementId("single_011");
        List<Element> children = new ArrayList<>();
        Element element_01 = new Element();
        element_01.setText("single_012");
        element_01.setElementId("single_012");
        Element element_02 = new Element();
        element_02.setText("single_013");
        element_02.setElementId("single_013");
        Element element_03 = new Element();
        element_03.setText("single_014");
        element_03.setElementId("single_014");
        Element element_04 = new Element();
        element_04.setText("single_015");
        element_04.setElementId("single_015");

        Element element_021 = new Element();
        element_021.setText("single_0212");
        element_021.setElementId("single_0212");
        Element element_022 = new Element();
        element_022.setText("single_0213");
        element_022.setElementId("single_0213");
        Element element_023 = new Element();
        element_023.setText("single_0214");
        element_023.setElementId("single_0214");
        List<Element> ele2children = new ArrayList<>();
        ele2children.add(element_021);
        ele2children.add(element_022);
        ele2children.add(element_023);
        element_02.setChildren(ele2children);

        children.add(element_01);
        children.add(element_02);
        children.add(element_03);
        children.add(element_04);
        element.setChildren(children);
        rootList.add(element);

        return rootList;
    }

    private Element updateMap() {

        Element element_02 = new Element();
        element_02.setText("single_014");
        element_02.setElementId("single_014");

        Element element_021 = new Element();
        element_021.setText("single_0312");
        element_021.setElementId("single_0312");
        Element element_022 = new Element();
        element_022.setText("single_0313");
        element_022.setElementId("single_0313");
        Element element_023 = new Element();
        element_023.setText("single_0314");
        element_023.setElementId("single_0314");
        List<Element> ele2children = new ArrayList<>();
        ele2children.add(element_021);
        ele2children.add(element_022);
        ele2children.add(element_023);
        element_02.setChildren(ele2children);

        return element_02;
    }

    private void writeToFile(List<Element> rootList) {
        jsonParser.elementToJsonFile(rootList);
    }


}
