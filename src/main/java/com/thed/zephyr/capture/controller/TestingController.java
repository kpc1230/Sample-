package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
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
    
    @Autowired
    private ProjectService projectService;


    @GetMapping
    public ResponseEntity<?> getTestSessionStatus(final @RequestParam(value = "projectKey")  String projectKey, final @RequestParam(value = "issueId")  String issueId) throws CaptureValidationException {
        log.info("Start of getTestSessionStatus() -> params : issueId : " + issueId);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(Objects.isNull(auth) || !auth.isAuthenticated()) {
                throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.create.issue"));
            }
            CaptureProject project = projectService.getCaptureProject(projectKey);
            if(project == null) {
            	throw new CaptureValidationException(i18n.getMessage("session.project.key.invalid", new Object[]{projectKey}));
            }
            
            if(!permissionService.hasBrowsePermission(project.getId())) {
                throw new CaptureRuntimeException(HttpStatus.UNAUTHORIZED.toString(), i18n.getMessage("template.validate.create.cannot.browse.project"));
            }

            CaptureIssue captureIssue = issueService.searchPropertiesByJql(issueId,CaptureCustomFieldsUtils.getAllEntityPropertiesKey());
            if(Objects.isNull(captureIssue)) {
                throw new CaptureValidationException(HttpStatus.BAD_REQUEST.toString(), i18n.getMessage("session.issue.invalid"));
            }
            TestSectionResponse testSectionResponse = issueService.getIssueSessionDetails(captureIssue);
            log.info("End of getTestSessionStatus().");
            return ResponseEntity.ok(testSectionResponse);
        } catch(Exception ex) {
            log.error("Error in getTestSessionStatus() -> ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }
}
