package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.service.jira.issue.IssueFields;
import com.thed.zephyr.capture.service.jira.issue.ResourceId;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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


    @RequestMapping(value = "/issue-ext", method = RequestMethod.POST)
    public ResponseEntity<CaptureIssue> createIssue(final HttpServletRequest request, final @RequestParam(value = "testSessionId",required = false)  String testSessionId, @Valid @RequestBody final IssueCreateRequest createRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Issue Create Request for Issue : {}", createRequest.toString());
        if (!permissionService.hasCreateIssuePermission()) {
            throw new CaptureRuntimeException("You do not have access to create the issue. Please check with JIRA Admin");
        }
        IssueFields issueFields = createRequest.fields();
        String rid = createRequest.getRid();
        IssueInput issueInput = createIssueInput(issueFields,request);
        BasicIssue basicIssue = postJiraRestClient.getIssueClient().createIssue(issueInput).claim();
        Issue issue = issueService.getIssueObject(basicIssue.getKey());

        //Set Context Params
        captureContextIssueFieldsService.populateContextFields(request, issue, createRequest.getContext());

        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(),basicIssue.getKey(),basicIssue.getId(),CaptureUtil.getFullIconUrl(issue,host));
        if(StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            if (session != null) {
                sessionActivityService.addRaisedIssue(session, issue, new Date(issue.getCreationDate().getMillis()),host.getUserKey().get());
            }
        }
        return new ResponseEntity<>(captureIssue, HttpStatus.OK);
    }

    private IssueInput createIssueInput(IssueFields issueFields, HttpServletRequest request) {
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
        issueInputBuilder.setIssueTypeId(Long.valueOf(issueFields.issueType().id()));
        if(issueFields.assignee() != null) {
            issueInputBuilder.setAssigneeName(issueFields.assignee().id());
        }
        Project project = getJiraRestClient.getProjectClient().getProject(issueFields.project().id()).claim();
        issueInputBuilder.setProject(project);
        if(issueFields.components() != null) {
            List<String> components = issueFields.components().stream().map(ResourceId::id).collect(Collectors.toList());
            if(components != null && components.size() > 0) {
                List<BasicComponent> componentList = new ArrayList<>();
                project.getComponents().forEach(component -> {
                    if(components.contains(String.valueOf(component.getId()))) {
                        BasicComponent basicComponent = new BasicComponent(component.getSelf(), component.getId(),component.getName(),component.getDescription());
                        componentList.add(basicComponent);
                    }
                });
                issueInputBuilder.setComponents(componentList);
            }
        }

        issueInputBuilder.setSummary(issueFields.summary());
        issueInputBuilder.setDescription(issueFields.description());
        if(issueFields.priority() != null) {
            issueInputBuilder.setPriorityId(Long.valueOf(issueFields.priority().id()));
        }

        if(issueFields.environment() != null) {
            issueInputBuilder.setFieldValue("environment",issueFields.getEnvironment());
        }

        if(issueFields.labels() != null) {

            List<String> labels = new ArrayList<>();
            issueFields.labels().stream().forEach(label -> {
                labels.add(label.trim());
            });
            issueInputBuilder.setFieldValue("labels",labels);
        }


        if(issueFields.customFields() != null) {
            for(Long custId : issueFields.customFields().keySet()) {
                String[] values = issueFields.customFields().get(custId);
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("values", values);
                FieldInput parentField = new FieldInput(String.valueOf(custId), new ComplexIssueInputFieldValue(valueMap));
                issueInputBuilder.setFieldInput(parentField);
            }
        }

        if(issueFields.versions() != null) {
            List<String> affectVersions = issueFields.versions().stream().map(ResourceId::id).collect(Collectors.toList());
            if(affectVersions != null && affectVersions.size() > 0) {
                List<String> versionList = new ArrayList<>();
                project.getVersions().forEach(version -> {
                    if(affectVersions.contains(String.valueOf(version.getId()))) {
                        versionList.add(version.getName());
                    }
                });
                issueInputBuilder.setAffectedVersionsNames(versionList);
            }
        }

        if(issueFields.fixVersions() != null) {
            List<String> fixVersions = issueFields.fixVersions().stream().map(ResourceId::id).collect(Collectors.toList());
            if(fixVersions != null && fixVersions.size() > 0) {
                List<String> versionList = new ArrayList<>();
                project.getVersions().forEach(version -> {
                    if(fixVersions.contains(String.valueOf(version.getId()))) {
                        versionList.add(version.getName());
                    }
                });
                issueInputBuilder.setFixVersionsNames(versionList);
            }
        }

        if(issueFields.getWorklog() != null) {
            issueInputBuilder.setFieldValue("worklog",issueFields.getWorklog());
        }

        if(issueFields.getTimetracking() != null) {
            Map<String, Object> timeTracking = new HashMap<>();
            timeTracking.put("originalEstimate", issueFields.getTimetracking().getOriginalEstimate());
            issueInputBuilder.setFieldValue("timetracking",new ComplexIssueInputFieldValue(timeTracking));
        }

        if(issueFields.getDuedate() != null) {
            DateTimeFormatter inFormatter = DateTimeFormat.forPattern("d/MMM/yy");
            DateTime dateTime = inFormatter.parseDateTime(issueFields.getDuedate());
            issueInputBuilder.setDueDate(dateTime.toDateTime(DateTimeZone.UTC));
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
