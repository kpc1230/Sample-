package com.thed.zephyr.capture.service.jira;


import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Masud on 8/13/17.
 */
public interface IssueService {
	
    Issue getIssueObject(String issueIdOrKey);
   
    CaptureIssue getCaptureIssue(String issueIdOrKey);

    List<CaptureIssue> getCaptureIssuesByIds(List<Long> issueIds);
    
    List<CaptureIssue> getCaptureIssuesByIssueRaiseBean(List<IssueRaisedBean> issuesRaised);

    TestSectionResponse getIssueSessionDetails(CaptureIssue issue) throws JSONException;

    CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest) throws CaptureValidationException;

    void addComment(String issueKey, String comment) throws JSONException;

    CaptureIssue searchPropertiesByJql(String issueKey, String allProperties);
    void linkIssues(List<SessionServiceImpl.CompleteSessionIssueLink> issueLinks , AtlassianHostUser hostUser);
    void addTimeTrakingToIssue(Issue issue, DateTime sessionCreationOn, Long durationInMilliSeconds,String comment, AtlassianHostUser hostUser);
}
