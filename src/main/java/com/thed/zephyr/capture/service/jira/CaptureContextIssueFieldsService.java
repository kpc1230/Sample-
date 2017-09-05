package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
public interface CaptureContextIssueFieldsService {
    void populateContextFields(HttpServletRequest req, Issue issueInputBuilder, Map<String, String> context);
}
