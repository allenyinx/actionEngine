package com.airta.action.engine.parser;

import com.airta.action.engine.entity.report.Element;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonParser {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper OBJECT_MAPPER;

    @Value("${engine.share}")
    private String TreeJsonPath;

    public String objectToJSONString(Object jsonObject) {

        String jsonString = null;
        try {
            jsonString = OBJECT_MAPPER.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
            return "";
        }

        return jsonString;
    }

    public Object resolveIncomingMessage(String value, Class objectClass) {

        logger.info("message {} resolved. ", value);
        try {
            logger.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        /**
         * may incoming JSON message
         */
        if (isJSONValid(value)) {
            logger.info("## valid json input, convert to jenkinsObject");

            if (value.contains("\\")) {
                logger.info("## payload contains escape chars: {}", value);
                value = value.replaceAll("\\\\", "##");
            }

            return new Gson().fromJson(value, objectClass);
        } else {
            logger.warn("# Not standard JSON payloads, try formatting..");

            try {

                value = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(format(value));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                return null;
            }
            if (isJSONValid(value)) {
                logger.info("## valid json input, convert to jenkinsObject");

                return new Gson().fromJson(value, objectClass);
            } else {
                logger.warn("## not standard payloads, abort");
                return null;
            }
        }
    }

    /**
     * check if input JSON payload valid
     *
     * @param inputValue
     * @return boolean
     */
    public boolean isJSONValid(String inputValue) {
        inputValue = format(inputValue);
        try {
            new JSONObject(inputValue);
        } catch (JSONException ex) {
            try {
                new JSONArray(inputValue);
            } catch (JSONException ex1) {
                return false;
            }
        }
        logger.info("## verified JSON input.");
        return true;
    }

    public boolean isJSONArray(String inputValue) {

        inputValue = format(inputValue);
        try {
            new JSONArray(inputValue);
        } catch (JSONException ex1) {
            return false;
        }
        logger.info("## verified JSON Array input.");
        return true;
    }

    private String format(String input) {

        return StringUtils.trimAllWhitespace(input);
    }

    public void updateExistingElementWithNewMessage(Element incomingRootElement) {

        File oldSitemapFile = new File(TreeJsonPath);
        if (!oldSitemapFile.exists()) {
            logger.info("## Previous sitemap JSON file not exists, initiate it.");
            List elementList = new ArrayList();
            elementList.add(incomingRootElement);
            elementToJsonFile(elementList, oldSitemapFile);
        } else {
            logger.info(" Updating previous sitemap ..");
            findAndUpdateTobeUpdatedRootElementFromOldSitemap(oldSitemapFile, incomingRootElement);

        }
    }

    private Element findAndUpdateTobeUpdatedRootElementFromOldSitemap(File oldSiteMapFile, Element incomingRootElement) {

        logger.info("Locating matching element by incoming element from previous sitemap");
        ObjectMapper mapper = new ObjectMapper();
        try {
            List obj = mapper.readValue(oldSiteMapFile, new TypeReference<List<Element>>(){});
            if (obj != null) {
                List<Element> oldElementList = (List<Element>) obj;
                logger.info("## Element list children size: {}", oldElementList.size());
                if (oldElementList.size() > 0) {
                    Element oldRootElement = oldElementList.get(0);
                    Element matchingElement = findFromSpecifiedOldElement(oldRootElement, incomingRootElement);
                    if (matchingElement != null) {
                        logger.info("## Found match element: {} from existing sitemap.", matchingElement.getElementId());
                        replaceElementWithIncoming(matchingElement, incomingRootElement);
                        elementToJsonFile(oldElementList, oldSiteMapFile);
                    } else {
                        logger.warn("## Cannot locate matching element ..");
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return null;
    }

    private Element findFromSpecifiedOldElement(Element oldStartElement, Element incomingRootElement) {

        if (compareElementsIfMatching(oldStartElement, incomingRootElement)) {
            logger.info("## start element matching, update the start one.");
            return oldStartElement;
        } else {
            List<Element> oldChildrenList = oldStartElement.getChildren();
            if(oldChildrenList.isEmpty()) {
                return null;
            }
            for (Element oldChildElement : oldChildrenList) {
                Element matchingElement = findFromSpecifiedOldElement(oldChildElement, incomingRootElement);
                if(matchingElement==null) {
                    continue;
                } else {
                    return matchingElement;
                }
            }
        }
        return null;
    }

    private boolean compareElementsIfMatching(Element oldElement, Element incomingElement) {

        if (oldElement.getElementId() != null && incomingElement.getElementId() != null) {
            return oldElement.getElementId().equals(incomingElement.getElementId());
        } else if (oldElement.getPathPath() != null && incomingElement.getPathPath() != null) {
            return oldElement.getPathPath().equals(incomingElement.getPathPath());
        } else {
            logger.warn("Element id or path NULL, compare fail.");
            return false;
        }
    }

    private void replaceElementWithIncoming(Element oldElement, Element newElement) {

        oldElement.setChildren(newElement.getChildren());
        oldElement.setChildrenCount(newElement.getChildrenCount());
        oldElement.setText(newElement.getText());
        oldElement.setElementId(newElement.getElementId());
        oldElement.setActionable(newElement.isActionable());
        oldElement.setWorkingOn(newElement.isWorkingOn());
        oldElement.setPathPath(newElement.getPathPath());
        oldElement.setParentId(newElement.getParentId());
        oldElement.setUrl(newElement.getUrl());
        oldElement.setType(newElement.getType());
        oldElement.setId(newElement.getId());
    }

    /**
     * export incoming element to JSON file as sitemap.
     *
     * @param rootElement
     */
    public void elementToJsonFile(List rootElement, File siteMapFile) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(siteMapFile, rootElement);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void elementToJsonFile(List rootElement) {

        elementToJsonFile(rootElement, new File(TreeJsonPath));
    }

}
