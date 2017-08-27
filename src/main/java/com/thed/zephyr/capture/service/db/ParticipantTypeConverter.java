package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by aliakseimatsarski on 8/24/17.
 */
public class ParticipantTypeConverter implements DynamoDBTypeConverter<String, Participant> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public String convert(Participant participant) {
        return participant != null?participant.toJSON().toString():null;
    }

    @Override
    public Participant unconvert(String jsonstr) {
        if(jsonstr == null){
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            Participant participant = om.readValue(jsonstr, Participant.class);

            return participant;
        } catch (IOException e) {
            log.error("Error during parsing Participant object.", e);
        }
        return null;
    }
}
