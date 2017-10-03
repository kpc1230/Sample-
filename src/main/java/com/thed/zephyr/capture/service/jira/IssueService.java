package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import org.codehaus.jettison.json.JSONException;

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

    CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest);

    void setIssueTestStausAndTestSession(String issueKey, String testingStatus,String sessionids);

    void addComment(String issueKey, String comment) throws JSONException;

    CaptureIssue searchPropertiesByJql(String issueKey, String allProperties);
}
