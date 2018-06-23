package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Session;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
public interface CaptureContextIssueFieldsService {

    void populateContextFields(HttpServletRequest req, Issue issueInputBuilder, Map<String, String> context);

    String getContextFields(String baseUrl, String path, String key) throws JSONException;

    void addRaisedInIssueField(String loggedUser, List<Long> listOfIssueIds, Session session);

    void addSessionContextIntoRaisedIssue(AcHostModel acHostModel, String userKey, Long issueId, Session session);

    void removeRaisedIssue(Session loadedSession, String issueKey);

    void populateIssueTestStatusAndTestSessions(String issueKey, String testStatus, String jiraPropIndexs, String baseUrl);
}
