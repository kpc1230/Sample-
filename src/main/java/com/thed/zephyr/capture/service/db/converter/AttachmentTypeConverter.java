package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.jira.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by aliakseimatsarski on 8/24/17.
 */
public class AttachmentTypeConverter implements DynamoDBTypeConverter<String, Attachment> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public String convert(Attachment attachment) {
        return attachment != null?attachment.toJSON().toString():null;
    }

    @Override
    public Attachment unconvert(String jsonstr) {
        ObjectMapper om = new ObjectMapper();
        try {
            Attachment participant = om.readValue(jsonstr, Attachment.class);

            return participant;
        } catch (IOException e) {
            log.error("Error during parsing Participant object.", e);
        }
        return null;
    }
}
