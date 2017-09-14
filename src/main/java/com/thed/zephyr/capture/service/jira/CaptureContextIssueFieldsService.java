package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
public interface CaptureContextIssueFieldsService {
    void populateContextFields(HttpServletRequest req, Issue issueInputBuilder, Map<String, String> context);

    String getContextFields(String baseUrl, String path,String key) throws JSONException;
}
