package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.SessionActivityItem;
import com.thed.zephyr.capture.model.jira.Issue;
import com.thed.zephyr.capture.model.jira.Project;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
public class SessionActivityItemCollectionConverter implements DynamoDBTypeConverter<Set<String>, Collection<SessionActivityItem>> {

    @Autowired
    private Logger log;

    @Override
    public Set<String> convert(Collection<SessionActivityItem> sessionActivityItems) {
        Set<String> result = new TreeSet<>();
        for (SessionActivityItem sessionActivityItem:sessionActivityItems){
            result.add(sessionActivityItem.getId());
        }
        return result;
    }

    @Override
    public Collection<SessionActivityItem> unconvert(Set<String> sessionActivityItemIds) {
        Collection<SessionActivityItem> result = new ArrayList<>();
        for (String sessionActivityItemId: sessionActivityItemIds){
            //Should be deleted after repository will be implemented.
            try{
                throw new Exception();
            } catch (Exception exception){
                log.error("The SessionActivityItem repository needs to be implemented", exception);
            }
        }
        return result;
    }
}
