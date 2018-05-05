package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by niravshah on 8/25/17.
 */
public interface AttachmentService {
     String addAttachments(MultipartFile[] multipartFiles, String issueIdOrKey) throws CaptureValidationException;

     String addAttachments(String issueKey, String testSessionId, JSONArray json) throws CaptureRuntimeException, JSONException, RestClientException, CaptureValidationException;
     String addAttachmentsByThreads(String issueKey, String testSessionId, JSONArray json) throws CaptureRuntimeException, JSONException, RestClientException, IOException, CaptureValidationException;
}
