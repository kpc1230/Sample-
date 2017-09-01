package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Venkatareddy on 9/01/17.
 */
public class TemplateTypeConverter implements DynamoDBTypeConverter<String, Template> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public String convert(Template template) {
        return template != null?template.toJSON().toString():null;
    }

    @Override
    public Template unconvert(String jsonstr) {
        ObjectMapper om = new ObjectMapper();
        try {
        	Template participant = om.readValue(jsonstr, Template.class);

            return participant;
        } catch (IOException e) {
            log.error("Error during parsing Template object.", e);
        }
        return null;
    }
}
