package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/26/17.
 */
public class ParticipantCollectionConverter implements DynamoDBTypeConverter<Set<String>, Collection<Participant>> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public Set<String> convert(Collection<Participant> participants) {
        if(participants == null){
            return null;
        }
        Set<String> result = new TreeSet<>();
        for (Participant participant:participants){
            result.add(participant.toJSON().toString());
        }
        return result;
    }

    @Override
    public Collection<Participant> unconvert(Set<String> participantsStr) {
        if(participantsStr == null){
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        Collection<Participant> result = new ArrayList();
        for (String jsonStr:participantsStr){
            try {
                Participant participant = om.readValue(jsonStr, Participant.class);
                result.add(participant);
            } catch (IOException e) {
                log.error("Error during parsing Participant object.", e);
            }
        }
        return result;
    }
}
