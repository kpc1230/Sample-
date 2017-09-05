package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by aliakseimatsarski on 9/1/17.
 */
public class JsonNodeTypeConverter implements DynamoDBTypeConverter<String, JsonNode> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public String convert(JsonNode jsonNode) {
        return jsonNode != null?jsonNode.toString():null;
    }

    @Override
    public JsonNode unconvert(String jsonString) {
        if (jsonString == null){
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try{
            JsonNode jsonNode = om.readValue(jsonString, JsonNode.class);

            return jsonNode;
        } catch (IOException e) {
            log.error("Error during parsing Tag object.", e);
        }
        return null;
    }
}
