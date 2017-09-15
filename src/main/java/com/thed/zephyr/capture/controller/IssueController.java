package com.thed.zephyr.capture.controller;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.PermissionService;
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

import java.util.Objects;

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
    private IssueService issueService;

    @Autowired
    @Qualifier("jiraRestClientPOST")
    private JiraRestClient postJiraRestClient;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CaptureI18NMessageSource i18n;


    @RequestMapping(value = "/issue-ext", method = RequestMethod.POST)
    public ResponseEntity<CaptureIssue> createIssue(final HttpServletRequest request, final @RequestParam(value = "testSessionId",required = false)  String testSessionId, @Valid @RequestBody final IssueCreateRequest createRequest) {
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

    @RequestMapping(value = "/testSessionStatus", method = RequestMethod.GET)
    public ResponseEntity<?> getTestSessionStatus(final @RequestParam(value = "issueKey")  String issueKey) throws CaptureValidationException {
    	log.info("Start of getTestSessionStatus() -> params : issueKey : " + issueKey);
        try {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(Objects.isNull(auth) || !auth.isAuthenticated()) {
                throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
            }
            Issue issue = issueService.getIssueObject(issueKey);
            if(Objects.isNull(issue)) {
                throw new CaptureValidationException(HttpStatus.BAD_REQUEST.toString(), i18n.getMessage("session.issue.invalid"));
            }
            TestSectionResponse testSectionResponse = issueService.getIssueSessionDetails(issue);
            log.info("End of getTestSessionStatus().");
            return ResponseEntity.ok(testSectionResponse);
        } catch(Exception ex) {
        	log.error("Error in getTestSessionStatus() -> ", ex);
        	throw new CaptureRuntimeException(ex);
        }
    }

}
