package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by niravshah on 8/25/17.
 */
public interface AttachmentService {
     String addAttachments(MultipartFile[] multipartFiles, String issueIdOrKey);

     String addAttachments(String issueKey, String testSessionId, JSONArray json) throws CaptureRuntimeException, JSONException, RestClientException ;
}
