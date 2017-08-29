package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.service.jira.AttachmentService;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by niravshah on 8/25/17.
 */
@RestController
public class AttachmentController {

    @Autowired
    private Logger log;

    @Autowired
    private AttachmentService attachmentService;

    @RequestMapping(value = "/issue-attach-new", method = RequestMethod.POST)
    public ResponseEntity uploadAttachments(final @RequestParam("issueKey") String issueKey,
                                           @RequestParam("files") MultipartFile[] multipartFiles) {
        String fullIconUrl = attachmentService.addAttachments(multipartFiles,issueKey);
        log.info("uploadAttachments() --> for IssueKey {} " + issueKey);
        return new ResponseEntity<Object>(fullIconUrl,HttpStatus.OK);
    }

    @RequestMapping(value = "/issue-attach", method = RequestMethod.POST)
    public ResponseEntity uploadAttachments(final @RequestParam("issueKey") String issueKey,
                                            @RequestBody String requestBody) {
        log.info("uploadAttachments() --> for IssueKey {} " + requestBody);
        final JSONArray json;
        try {
            json = new JSONArray(requestBody);
            attachmentService.addAttachments(issueKey, json);
        } catch (JSONException e) {
            throw new CaptureRuntimeException("rest.resource.malformed.json",e);

        }
        return new ResponseEntity<Object>(HttpStatus.OK);
    }
}
