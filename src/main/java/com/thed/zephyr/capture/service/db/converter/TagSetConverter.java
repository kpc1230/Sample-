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
public class TagSetConverter implements DynamoDBTypeConverter<Set<String>, Set<String>> {

    private static Logger log = LoggerFactory.getLogger("application");

    @Override
    public Set<String> convert(Set<String> tags) {

        return tags.size()>0?tags:null;
    }

    @Override
    public Set<String> unconvert(Set<String> tags) {

        return tags != null?tags:new TreeSet<>();
    }
}
