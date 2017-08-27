package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.atlassian.jira.rest.client.api.domain.Issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/26/17.
 */
public class LongCollectionConverter implements DynamoDBTypeConverter<Set<Long>, Collection<Long>> {
    @Override
    public Set<Long> convert(Collection<Long> longCollection) {
        if(longCollection == null){
            return null;
        }
        Set<Long> result = new TreeSet<>();
        for (Long longValue:longCollection){
            result.add(longValue);
        }
        return result;
    }

    @Override
    public Collection<Long> unconvert(Set<Long> longSetCollection) {
        if(longSetCollection == null){
            return null;
        }
        Collection<Long> result = new ArrayList();
        for (Long longValue:longSetCollection){
            result.add(longValue);
        }
        return result;
    }
}
