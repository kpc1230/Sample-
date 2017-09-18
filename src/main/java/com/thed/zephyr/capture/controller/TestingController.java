package com.thed.zephyr.capture.controller;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Created by niravshah on 9/17/17.
 */
@RestController
@RequestMapping("testing")
public class TestingController {
    @Autowired
    private Logger log;

    @Autowired
    private IssueService issueService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CaptureI18NMessageSource i18n;



    @GetMapping
    public ResponseEntity<?> getTestSessionStatus(final @RequestParam(value = "projectKey")  String projectKey, final @RequestParam(value = "issueId")  String issueId) throws CaptureValidationException {
        log.info("Start of getTestSessionStatus() -> params : issueId : " + issueId);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(Objects.isNull(auth) || !auth.isAuthenticated()) {
                throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
            }
            if(!permissionService.hasBrowsePermission(projectKey)) {
                throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.browse.project"));
            }
            Issue issue = issueService.getIssueObject(issueId);
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
