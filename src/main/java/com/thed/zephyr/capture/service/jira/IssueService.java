package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.Issue;
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
	
    Issue getIssueObject(Long issueId);
    
    Issue getIssueObject(String issueKey);

    CaptureIssue getCaptureIssue(Long issueId);

    List<CaptureIssue> getCaptureIssuesByIds(List<Long> issueIds);

    TestSectionResponse getIssueSessionDetails(Issue issue) throws JSONException;

    CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest);
}
