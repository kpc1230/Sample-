package com.thed.zephyr.capture.service.jira;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
public interface CaptureContextIssueFieldsService {
    void populateContextFields(HttpServletRequest request, String key, Map<String, String> context);
}
