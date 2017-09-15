package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureEnvironment;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.model.jira.TestingStatus;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.service.jira.issue.IssueFields;
import com.thed.zephyr.capture.service.jira.issue.ResourceId;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Autowired
    private JiraRestClient getJiraRestClient;

    @Autowired
    @Qualifier("jiraRestClientPOST")
    private JiraRestClient postJiraRestClient;

    @Autowired
    private CaptureContextIssueFieldsService captureContextIssueFieldsService;

    @Autowired
    private SessionActivityService sessionActivityService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private DynamoDBAcHostRepository dynamoDBAcHostRepository;

    @Autowired
    private CaptureI18NMessageSource i18n;


    @Override
    public Issue getIssueObject(Long issueId) {
    	return jiraRestClient.getIssueClient().getIssue(String.valueOf(issueId)).claim();
    }
    
    @Override
    public Issue getIssueObject(String issueKey) {
        return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
    }

    /**
     * Get serialized issue
     * @param issueId
     * @return
     */
    @Override
    public CaptureIssue getCaptureIssue(Long issueId) {
        Issue issue = getIssueObject(issueId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.debug("ISSUE: --> {}",issue.getSummary());
        return new CaptureIssue(issue.getSelf(),
                issue.getKey(),issue.getId(),
                CaptureUtil.getFullIconUrl(issue,host), issue.getSummary());
    }

    @Override
    public List<CaptureIssue> getCaptureIssuesByIds(List<Long> issueIds) {
        List<CaptureIssue> captureIssues = new ArrayList<>();
        if(issueIds != null && issueIds.size()>0) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
            String jql = "issue in (" + StringUtils.join(issueIds, ',') + ")";
            SearchResult searchResultPromise =
                    jiraRestClient.getSearchClient().searchJql(jql).claim();
            searchResultPromise.getIssues()
                    .forEach(issue -> {
                        captureIssues.add(new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host), issue.getSummary()));
                    });
            }
            return captureIssues;
        }

    @Override
    public TestSectionResponse getIssueSessionDetails(Issue issue) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        SessionSearchList sessionByRelatedIssueId = sessionService.getSessionByRelatedIssueId(CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository), issue.getProject().getId(), issue.getId());
        TestSectionResponse testSectionResponse = new TestSectionResponse();
        testSectionResponse.setSessions(sessionByRelatedIssueId.getContent());
        String userAgentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ","_");
        String userAgent = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),userAgentPath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ","_"));

        String browserNamePath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ","_");
        String browser = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),browserNamePath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ","_"));

        String documentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ","_");
        String document = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),documentPath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ","_"));

        String screenPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ","_");
        String screen = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),screenPath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ","_"));

        String operatingSystemPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase().replace(" ","_");
        String operatingSystem = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),operatingSystemPath,CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase().replace(" ","_"));

        String urlPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ","_");
        String url = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),urlPath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ","_"));

        String jQueryVersionPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ","_");
        String jQueryVersion = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(),jQueryVersionPath,CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ","_"));


        CaptureEnvironment captureEnvironment = new CaptureEnvironment();
        captureEnvironment.setUserAgent(userAgent);
        captureEnvironment.setBrowser(browser);
        captureEnvironment.setjQueryVersion(jQueryVersion);
        captureEnvironment.setDocumentMode(document);
        captureEnvironment.setScreenResolution(screen);
        captureEnvironment.setUrl(url);
        captureEnvironment.setOperatingSystem(operatingSystem);
        testSectionResponse.setCaptureEnvironment(captureEnvironment);

        TestingStatus testingStatus = new TestingStatus();
        testingStatus.setTotalCount(sessionByRelatedIssueId.getContent() != null ? sessionByRelatedIssueId.getContent().size() : new Double(0));
        AtomicInteger startedCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        AtomicInteger inProgressCount = new AtomicInteger();
        AtomicInteger notStartedCount = new AtomicInteger();

        sessionByRelatedIssueId.getContent().stream().peek(session -> {
            if (session != null) {
                if (Session.Status.STARTED.equals(session.getStatus())) {
                    startedCount.getAndIncrement();
                } else if (Session.Status.CREATED.equals(session.getStatus())) {
                    notStartedCount.getAndIncrement();
                } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
                    completedCount.getAndIncrement();
                } else {
                    // If a status other than created and completed appears, then it is in progress
                    inProgressCount.getAndIncrement();
                }
            }
        });
        if (startedCount.get() == 0 && completedCount.get() != 0) {
            // If all the sessions are 'completed' then return complete
            testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.COMPLETED.getI18nKey()));
        } else if (startedCount.get() != 0 && completedCount.get() == 0) {
            // If all the sessions are 'created' then return not started
            testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.NOT_STARTED.getI18nKey()));
        } else {
            // Otherwise the sessions are either 'completed' or 'created'
            testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.INCOMPLETE.getI18nKey()));
        }
        testingStatus.setCompleteCount(new Double(completedCount.get()));
        Double notStartedPercent = Math.floor((notStartedCount.get() / testingStatus.getTotalCount().intValue()) * 100);
        Double inProgressPercent = Math.floor((inProgressCount.get() / testingStatus.getTotalCount().intValue()) * 100);
        Double completePercent = Math.floor((completedCount.get() / testingStatus.getTotalCount().intValue()) * 100);

        testingStatus.setNotStartedPercent(notStartedPercent);
        testingStatus.setInProgressPercent(inProgressPercent);
        testingStatus.setCompletePercent(completePercent);
        testingStatus.setTotalSessions(testingStatus.getTotalSessions());
        testSectionResponse.setTestingStatus(testingStatus);
        return testSectionResponse;
    }

    @Override
    public CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        IssueFields issueFields = createRequest.fields();
        String rid = createRequest.getRid();
        IssueInput issueInput = createIssueInput(issueFields,request);
        BasicIssue basicIssue = postJiraRestClient.getIssueClient().createIssue(issueInput).claim();
        Issue issue = getIssueObject(basicIssue.getKey());

        //Set Context Params
        captureContextIssueFieldsService.populateContextFields(request, issue, createRequest.getContext());

        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(),basicIssue.getKey(),basicIssue.getId(),CaptureUtil.getFullIconUrl(issue,host),issue.getSummary());
        if(StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            if (session != null) {
                sessionActivityService.addRaisedIssue(session, issue, new Date(issue.getCreationDate().getMillis()),host.getUserKey().get());
            }
        }
        return captureIssue;
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
            Issue issue = getIssueObject(parentId.id());
            Map<String, Object> parent = new HashMap<>();
            parent.put("key", issue.getKey());
            FieldInput parentField = new FieldInput("parent", new ComplexIssueInputFieldValue(parent));
            issueInputBuilder.setFieldInput(parentField);
        }

        return issueInputBuilder.build();
    }

}
