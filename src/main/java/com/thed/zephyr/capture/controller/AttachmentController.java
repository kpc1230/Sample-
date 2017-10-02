package com.thed.zephyr.capture.controller;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.model.ErrorDto;
import com.thed.zephyr.capture.service.jira.AttachmentService;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by niravshah on 8/25/17.
 */
@RestController
public class AttachmentController {

    @Autowired
    private Logger log;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private CaptureI18NMessageSource i18n;

    @RequestMapping(value = "/issue-attach-new", method = RequestMethod.POST)
    public ResponseEntity<String> uploadAttachments(final @RequestParam("issueKey") String issueKey,
                                           @RequestParam("files") MultipartFile[] multipartFiles) {
        String fullIconUrl = attachmentService.addAttachments(multipartFiles,issueKey);
        log.info("uploadAttachments() --> for IssueKey {} " + issueKey);
        return new ResponseEntity<>(fullIconUrl,HttpStatus.OK);
    }

    @RequestMapping(value = "/issue-attach", method = RequestMethod.POST)
    public ResponseEntity<?> uploadAttachments(final @RequestParam("issueKey") String issueKey,
                                                    final @RequestParam(value = "testSessionId",required = false) String testSessionId,@RequestBody String requestBody) {
        log.info("uploadAttachments() --> for IssueKey {} " + requestBody);
        final JSONArray json;
        try {
            json = new JSONArray(requestBody);
            String fullIconUrl = attachmentService.addAttachments(issueKey,testSessionId,json);
            return new ResponseEntity<>(new AttachmentResponse(fullIconUrl),HttpStatus.OK);
        } catch(CaptureRuntimeException e) {
            log.error("Error adding attachment",e);
            throw e;
        } catch (JSONException e) {
            log.error("JSON Error adding attachment",e);
            throw new CaptureRuntimeException(i18n.getMessage("rest.resource.malformed.json"),e);
        } catch(RestClientException e) {
            log.error("Error adding attachment",e);
            JSONArray errorArray = new JSONArray();
            try {
                e.getErrorCollections().stream().forEach(errorCollection -> {
                    errorCollection.getErrorMessages().stream().forEach(errorMessage -> {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            if(StringUtils.startsWith(errorMessage,"Issue does not exist")) {
                                jsonObject.put("errorMessage", i18n.getMessage("file.error.issue.key.invalid",new Object[]{issueKey}));
                            } else {
                                jsonObject.put("errorMessage", errorMessage);
                            }
                            errorArray.put(jsonObject);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    });
                });
                JSONObject responseJson = new JSONObject();
                responseJson.put("errors",errorArray);
                return new ResponseEntity<>(responseJson.toString(),HttpStatus.BAD_REQUEST);
            } catch (JSONException jsonEx) {
                log.error("Error Adding Value to JSON",jsonEx);
            }
        } catch(Exception e) {
            log.error("Error adding attachment",e);
            throw new CaptureRuntimeException(e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    public class AttachmentResponse implements Serializable {
        @JsonProperty
        private String iconPath;

        public AttachmentResponse() {
        }

        public AttachmentResponse(String iconPath) {
            this.iconPath = iconPath;
        }

        public String getIconPath() {
            return iconPath;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }
    }
}
