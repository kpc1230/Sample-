package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/27/17.
 */
public class TagSetConverter implements DynamoDBTypeConverter<Set<String>, Set<Tag>> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public Set<String> convert(Set<Tag> tags) {
        Set<String> result = new TreeSet<>();
        for (Tag tag : tags) {
            result.add(tag.toJson().toString());
        }
        return result;
    }

    @Override
    public Set<Tag> unconvert(Set<String> tagStringList) {
        Set<Tag> result = new TreeSet<>();
        ObjectMapper om = new ObjectMapper();
        for (String jsonStr:tagStringList){
            try {
                Tag tag = om.readValue(jsonStr, Tag.class);
                result.add(tag);
            } catch (IOException e) {
                log.error("Error during parsing Tag object.", e);
            }
        }
        return result;
    }
}
