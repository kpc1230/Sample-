package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.JiraIssueFieldService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.service.jira.issue.IssueFields;
import com.thed.zephyr.capture.service.jira.issue.ResourceId;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by niravshah on 8/28/17.
 */
@RestController
public class IssueController {

    @Autowired
    private Logger log;

    @Autowired
    private JiraIssueFieldService issueFieldService;

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


    @RequestMapping(value = "/issue-ext", method = RequestMethod.POST)
    public ResponseEntity<BasicIssue> createIssue(final HttpServletRequest request, final @RequestParam(value = "testSessionId",required = false)  String testSessionId, @Valid @RequestBody final IssueCreateRequest createRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Issue Create Request for Issue : {}", createRequest.toString());
        if (!permissionService.hasCreateIssuePermission()) {
            throw new CaptureRuntimeException("You do not have access to create the issue. Please check with JIRA Admin");
        }
        IssueFields issueFields = createRequest.fields();
        String rid = createRequest.getRid();
        IssueInput issueInput = createIssueInput(issueFields);
        BasicIssue basicIssue = postJiraRestClient.getIssueClient().createIssue(issueInput).claim();
        Issue issue = issueService.getIssueObject(basicIssue.getKey());
        //captureContextIssueFieldsService.populateContextFields(request, basicIssue.getKey(), createRequest.getContext());
        if(StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            if (session != null) {
                sessionActivityService.addRaisedIssue(session,issue,issue.getCreationDate(),host.getUserKey().get());
            }
        }
        return new ResponseEntity<>(basicIssue, HttpStatus.OK);
    }

    private IssueInput createIssueInput(IssueFields issueFields) {
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
        issueInputBuilder.setIssueTypeId(Long.valueOf(issueFields.issueType().id()));
        issueInputBuilder.setAssigneeName(issueFields.assignee().id());
        if(issueFields.components() != null) {
            List<String> componentNames = issueFields.components().stream().map(ResourceId::id).collect(Collectors.toList());
            if(componentNames != null && componentNames.size() > 0) {
                issueInputBuilder.setComponentsNames(componentNames);
            }
        }

        Project project = getJiraRestClient.getProjectClient().getProject(issueFields.project().id()).claim();
        issueInputBuilder.setProject(project);
        issueInputBuilder.setSummary(issueFields.summary());
        issueInputBuilder.setDescription(issueFields.description());
        issueInputBuilder.setPriorityId(Long.valueOf(issueFields.priority().id()));

        if(issueFields.fixVersions() != null) {
            List<String> fixVersions = issueFields.fixVersions().stream().map(ResourceId::id).collect(Collectors.toList());
            if(fixVersions != null && fixVersions.size() > 0) {
                issueInputBuilder.setFixVersionsNames(fixVersions);
            }
        }
        ResourceId parentId = issueFields.parent();
        if(parentId != null) {
            Issue issue = issueService.getIssueObject(parentId.id());
            Map<String, Object> parent = new HashMap<>();
            parent.put("key", issue.getKey());
            FieldInput parentField = new FieldInput("parent", new ComplexIssueInputFieldValue(parent));
            issueInputBuilder.setFieldInput(parentField);
        }
        return issueInputBuilder.build();
    }
}
