package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.util.IssueSearchList;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.IssueSearchService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by niravshah on 8/28/17.
 */
@RestController

public class IssueController {

    @Autowired
    private Logger log;

    @Autowired
    private IssueService issueService;

    @Autowired
    @Qualifier("jiraRestClientPOST")
    private JiraRestClient postJiraRestClient;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CaptureI18NMessageSource i18n;
    
    @Autowired
    private IssueSearchService issueSearchService;

    @RequestMapping(value = "/issue-ext", method = RequestMethod.POST)
    public ResponseEntity<CaptureIssue> createIssue(final HttpServletRequest request, final @RequestParam(value = "testSessionId",required = false)  String testSessionId, @Valid @RequestBody final IssueCreateRequest createRequest) throws CaptureValidationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
        }
        log.info("Issue Create Request for Issue : {}", createRequest.toString());

        if (!permissionService.hasCreateIssuePermission()) {
            throw new CaptureRuntimeException(HttpStatus.FORBIDDEN.toString(),i18n.getMessage("template.validate.create.cannot.create.issue"));
        }
        CaptureIssue captureIssue = issueService.createIssue(request,testSessionId,createRequest);
        return new ResponseEntity<>(captureIssue, HttpStatus.OK);
    }

    @RequestMapping(value = "/issue/{issueKey}/comment", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addComments(@PathVariable(value = "issueKey",required = true)  String issueKey, @RequestBody String comment) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
        }
        log.info("Add Comment Request for Issue : {}", issueKey);
        if (!permissionService.hasEditIssuePermission(issueKey) || !permissionService.canAddCommentPermission(issueKey)) {
            throw new CaptureRuntimeException(HttpStatus.FORBIDDEN.toString(),i18n.getMessage("template.validate.create.cannot.create.issue"));
        }
        try {
            issueService.addComment(issueKey,comment);
        } catch(RestClientException e) {
            JSONArray errorArray = new JSONArray();
            e.getErrorCollections().stream().forEach(errorCollection -> {
                errorCollection.getErrorMessages().stream().forEach(errorMessage -> {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("errorMessage", errorMessage);
                        errorArray.put(jsonObject);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                });
            });
            JSONObject responseJson = new JSONObject();
            responseJson.put("errors",errorArray);
            return new ResponseEntity<>(responseJson.toString(),HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            log.error("Failed to add Comment to issue",e);
            throw new CaptureRuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),i18n.getMessage("capture.add.comment.error"));
        }
        return new ResponseEntity<>(new JSONObject().toString(), HttpStatus.OK);
    }
    
    @GetMapping(value = "/issueSearch/autocomplete")
    public ResponseEntity<?> getIssuesForQuery(@RequestParam("term")  String term, @RequestParam("projectKey") String projectKey) {
    	log.info("Start of getIssuesForQuery() --> params - query " + term + " projectKey " + projectKey);
    	IssueSearchList searchIssues = issueSearchService.getIssuesForQuery(projectKey, term);
        log.info("End of getIssuesForQuery()");
        return ResponseEntity.ok(searchIssues);
    }
    
    @GetMapping(value = "/issueSearch/autocompleteEpic")
    public ResponseEntity<?> getIssuesEpicForQuery(@RequestParam("term")  String term, @RequestParam  String projectKey) {
    	log.info("Start of getIssuesEpicForQuery() --> params - query " + term + " projectKey " + projectKey);
    	IssueSearchList searchIssues = issueSearchService.getEpicIssuesForQuery(projectKey, term);
        log.info("End of getIssuesEpicForQuery()");
        return ResponseEntity.ok(searchIssues);
    }
}
