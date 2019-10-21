package com.airta.action.engine.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JsonParser {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
}
