package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by niravshah on 8/28/17.
 */
@RestController
public class IssueController {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient getJiraRestClient;

    @Autowired
    private IssueService issueService;

    @Autowired
    @Qualifier("jiraRestClientPOST")
    private JiraRestClient postJiraRestClient;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CaptureContextIssueFieldsService captureContextIssueFieldsService;

    @Autowired
    private SessionActivityService sessionActivityService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CaptureI18NMessageSource i18n;


    @RequestMapping(value = "/issue-ext", method = RequestMethod.POST)
    public ResponseEntity<CaptureIssue> createIssue(final HttpServletRequest request, final @RequestParam(value = "testSessionId",required = false)  String testSessionId, @Valid @RequestBody final IssueCreateRequest createRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
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

    @RequestMapping(value = "/testSessionStatus", method = RequestMethod.GET)
    public ResponseEntity<TestSectionResponse> getTestSessionStatus(final @RequestParam(value = "issueKey")  String issueKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        if(auth == null || !auth.isAuthenticated()) {
            throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
        }
        log.info("getTestSessionStatus Request for Issue : {}", issueKey);
        Issue issue = issueService.getIssueObject(issueKey);
        if(issue == null) {
            throw new CaptureRuntimeException(HttpStatus.BAD_REQUEST.toString(),i18n.getMessage("session.issue.invalid"));
        }

        TestSectionResponse testSectionResponse = null;
        try {
            testSectionResponse = issueService.getIssueSessionDetails(issue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(testSectionResponse, HttpStatus.OK);
    }

}
