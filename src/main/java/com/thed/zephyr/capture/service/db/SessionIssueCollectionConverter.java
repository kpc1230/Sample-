package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.jira.Issue;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.jira.IssueService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
public class SessionIssueCollectionConverter  implements DynamoDBTypeConverter<Set<Long>, Collection<Issue>> {

    @Autowired
    private IssueService issueService;
    @Override
    public Set<Long> convert(Collection<Issue> issueCollection) {
        Set<Long> result = new TreeSet<>();
        for (Issue issue:issueCollection){
            result.add(issue.getId());
        }
        return result;
    }

    @Override
    public Collection<Issue> unconvert(Set<Long> issueIdsSet) {
        Collection<Issue> result = new ArrayList<>();
        for (Long issueId: issueIdsSet){
            result.add(issueService.getIssueObject(issueId));
        }
        return result;
    }
}
