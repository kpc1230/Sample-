package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.service.jira.IssueService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by aliakseimatsarski on 8/24/17.
 */
public class IssueTypeConverter implements DynamoDBTypeConverter<Long, Issue> {

    @Autowired
    private IssueService issueService;

    @Override
    public Long convert(Issue issue) {
        return issue != null?issue.getId():null;
    }

    @Override
    public Issue unconvert(Long issueId) {
        return issueId != null?issueService.getIssueObject(issueId):null;
    }
}
