package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureEnvironment;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.TestSectionResponse;
import com.thed.zephyr.capture.model.jira.TestingStatus;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.service.jira.issue.IssueFields;
import com.thed.zephyr.capture.service.jira.issue.ResourceId;
import com.thed.zephyr.capture.util.*;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
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
import java.util.concurrent.Callable;
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

    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private DynamicProperty dynamicProperty;

    @Override
    public Issue getIssueObject(String issueIdOrKey) {
        return jiraRestClient.getIssueClient().getIssue(issueIdOrKey).claim();
    }

    /**
     * Get serialized issue
     *
     * @param issueIdOrKey
     * @return
     */
    @Override
    public CaptureIssue getCaptureIssue(String issueIdOrKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        CaptureIssue captureIssue = null;
        try {
            captureIssue = tenantAwareCache.getOrElse(acHostModel, buildIssueCacheKey(issueIdOrKey), new Callable<CaptureIssue>() {
                @Override
                public CaptureIssue call() throws Exception {
                    Issue issue = getIssueObject(issueIdOrKey);
                    return new CaptureIssue(issue.getSelf(),
                            issue.getKey(), issue.getId(),
                            CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName());
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.ISSUE_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exp) {
            log.error("Exception while getting the issue from JIRA." + exp.getMessage(), exp);
        }
        log.debug("ISSUE: --> {}", captureIssue.getSummary());
        return captureIssue;
    }

    @Override
    public List<CaptureIssue> getCaptureIssuesByIds(List<Long> issues) {
        List<CaptureIssue> captureIssues = new ArrayList<>();
        if (issues != null && issues.size() > 0) {
            String jql = "issue in (" + StringUtils.join(issues, ',') + ")";
            captureIssues = getCaptureIssuesForJQL(jql);
        }
        return captureIssues;
    }
    
    @Override
    public List<CaptureIssue> getCaptureIssuesByIssueRaiseBean(List<IssueRaisedBean> issues) {
        List<CaptureIssue> captureIssues = new ArrayList<>();
        if (issues != null && issues.size() > 0) {
            StringBuilder issueIds = new StringBuilder();
        	int count = issues.size();
            int index= 1;
            for(IssueRaisedBean tempIssueRaisedBean : issues) {
            	if(index < count) {
            		issueIds.append(tempIssueRaisedBean.getIssueId()).append(",");
            	} else {
            		issueIds.append(tempIssueRaisedBean.getIssueId());
            	}
            	index++;
            }
            String jql = "issue in (" + issueIds.toString() + ")";
            captureIssues = getCaptureIssuesForJQL(jql);
        }
        return captureIssues;
    }
    
    private List<CaptureIssue> getCaptureIssuesForJQL(String jql) {
    	List<CaptureIssue> captureIssues = new ArrayList<>();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        SearchResult searchResultPromise =
                jiraRestClient.getSearchClient().searchJql(jql).claim();
        searchResultPromise.getIssues()
                .forEach(issue -> {
                    captureIssues.add(new CaptureIssue(issue.getSelf(),
                            issue.getKey(), issue.getId(),
                            CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName()));
                });
        return captureIssues;
    }

    @Override
    public TestSectionResponse getIssueSessionDetails(Issue issue) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String ctdId = CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository);
        Session raisedDuring = sessionService.getSessionRaisedDuring(ctdId, issue.getId());
        SessionDtoSearchList sessionByRelatedIssueId = sessionService.getSessionByRelatedIssueId(host.getUserKey().get(), ctdId, issue.getProject().getId(), issue.getId());
        TestSectionResponse testSectionResponse = new TestSectionResponse();
        testSectionResponse.setSessions(sessionByRelatedIssueId.getContent());
        testSectionResponse.setRaisedDuring(raisedDuring);
        StringBuilder basePath = new StringBuilder();
        basePath.append(JiraConstants.REST_API_BASE_ISSUE).append("/").append(issue.getKey()).append("/properties/");
        String userAgentPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ", "_");
        String userAgent = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), userAgentPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ", "_"));

        String browserNamePath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ", "_");
        String browser = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), browserNamePath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ", "_"));

        String documentPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ", "_");
        String document = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), documentPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ", "_"));

        String screenPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ", "_");
        String screen = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), screenPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ", "_"));

        String operatingSystemPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase().replace(" ", "_");
        String operatingSystem = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), operatingSystemPath, CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase().replace(" ", "_"));

        String urlPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ", "_");
        String url = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), urlPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ", "_"));

        String jQueryVersionPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ", "_");
        String jQueryVersion = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), jQueryVersionPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ", "_"));


        CaptureEnvironment captureEnvironment = new CaptureEnvironment();
        captureEnvironment.setUserAgent(userAgent);
        captureEnvironment.setBrowser(browser);
        captureEnvironment.setjQueryVersion(jQueryVersion);
        captureEnvironment.setDocumentMode(document);
        captureEnvironment.setScreenResolution(screen);
        captureEnvironment.setUrl(url);
        captureEnvironment.setOperatingSystem(operatingSystem);
        captureEnvironment.setBrowserIcon(CaptureUtil.getBrowserIcon(browser));
        captureEnvironment.setOsIcon(CaptureUtil.getOSIcon(operatingSystem));
        testSectionResponse.setCaptureEnvironment(captureEnvironment);

        TestingStatus testingStatus = new TestingStatus();
        testingStatus.setTotalCount(sessionByRelatedIssueId.getContent() != null ? sessionByRelatedIssueId.getContent().size() : new Double(0));
        AtomicInteger startedCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        AtomicInteger inProgressCount = new AtomicInteger();
        AtomicInteger notStartedCount = new AtomicInteger();

        sessionByRelatedIssueId.getContent().stream().forEach(session -> {
            if (session != null) {
               /* if (Session.Status.STARTED.equals(session.getStatus())) {
                    startedCount.getAndIncrement();
                } else*/ if (Session.Status.CREATED.equals(session.getStatus())) {
                    notStartedCount.getAndIncrement();
                } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
                    completedCount.getAndIncrement();
                } else {
                    // If a status other than created and completed appears, then it is in progress
                    inProgressCount.getAndIncrement();
                }
            }
        });
        if(inProgressCount.get()==0){
            // If all the sessions are 'completed' then return complete
            if (notStartedCount.get() == 0 && completedCount.get() != 0) {
                testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.COMPLETED.getI18nKey()));
            } else if (notStartedCount.get() != 0 && completedCount.get() == 0) {
                // If all the sessions are 'created' then return not started
                testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.NOT_STARTED.getI18nKey()));
            } else {
                // Otherwise the sessions are either 'completed' or 'created'
                testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.INCOMPLETE.getI18nKey()));
            }
        }else{
            testingStatus.setTestingStatusEnum(i18n.getMessage(TestingStatus.TestingStatusEnum.IN_PROGRESS.getI18nKey()));
        }

        testingStatus.setCompleteCount(new Double(completedCount.get()));
        Double notStartedPercent = 0.0, inProgressPercent = 0.0, completePercent = 0.0;
        if (testingStatus.getTotalCount().intValue() != 0) {
            notStartedPercent = Math.floor((notStartedCount.get() / testingStatus.getTotalCount().intValue()) * 100);
            inProgressPercent = Math.floor((inProgressCount.get() / testingStatus.getTotalCount().intValue()) * 100);
            completePercent = Math.floor((completedCount.get() / testingStatus.getTotalCount().intValue()) * 100);
        }

        testingStatus.setNotStartedPercent(notStartedPercent);
        testingStatus.setInProgressPercent(inProgressPercent);
        testingStatus.setCompletePercent(completePercent);
        testingStatus.setTotalSessions(testingStatus.getTotalCount());
        testSectionResponse.setTestingStatus(testingStatus);
        return testSectionResponse;
    }

    @Override
    public CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        IssueFields issueFields = createRequest.fields();
        IssueInput issueInput = createIssueInput(issueFields, request);
        BasicIssue basicIssue = postJiraRestClient.getIssueClient().createIssue(issueInput).claim();
        Issue issue = getIssueObject(basicIssue.getKey());

        //Set Context Params
        captureContextIssueFieldsService.populateContextFields(request, issue, createRequest.getContext());

        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(), basicIssue.getKey(), basicIssue.getId(), CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName());
        if (StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            if (session != null) {
                sessionActivityService.addRaisedIssue(session, issue, new Date(issue.getCreationDate().getMillis()), host.getUserKey().get());
                List<Long> issueIds = new ArrayList<>();
                issueIds.add(issue.getId());
                captureContextIssueFieldsService.addRaisedInIssueField(host.getUserKey().get(), issueIds, session.getId());
            }
        }
        return captureIssue;
    }

    @Override
    public void setIssueTestStausAndTestSession(String issueId, String testingStatus,String sessionids){
        captureContextIssueFieldsService.populateIssueTestStatusAndTestSessions(getCaptureIssue(String.valueOf(issueId)).getKey(),i18n.getMessage(testingStatus),sessionids);
        return;
    }

    private IssueInput createIssueInput(IssueFields issueFields, HttpServletRequest request) {
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
        issueInputBuilder.setIssueTypeId(Long.valueOf(issueFields.issueType().id()));
        if (issueFields.assignee() != null) {
            issueInputBuilder.setAssigneeName(issueFields.assignee().id());
        }
        Project project = getJiraRestClient.getProjectClient().getProject(issueFields.project().id()).claim();
        issueInputBuilder.setProject(project);
        if (issueFields.components() != null) {
            List<String> components = issueFields.components().stream().map(ResourceId::id).collect(Collectors.toList());
            if (components != null && components.size() > 0) {
                List<BasicComponent> componentList = new ArrayList<>();
                project.getComponents().forEach(component -> {
                    if (components.contains(String.valueOf(component.getId()))) {
                        BasicComponent basicComponent = new BasicComponent(component.getSelf(), component.getId(), component.getName(), component.getDescription());
                        componentList.add(basicComponent);
                    }
                });
                issueInputBuilder.setComponents(componentList);
            }
        }

        issueInputBuilder.setSummary(issueFields.summary());
        issueInputBuilder.setDescription(issueFields.description());
        if (issueFields.priority() != null) {
            issueInputBuilder.setPriorityId(Long.valueOf(issueFields.priority().id()));
        }

        if (issueFields.environment() != null) {
            issueInputBuilder.setFieldValue("environment", issueFields.getEnvironment());
        }

        if (issueFields.labels() != null) {

            List<String> labels = new ArrayList<>();
            issueFields.labels().stream().forEach(label -> {
                labels.add(label.trim());
            });
            issueInputBuilder.setFieldValue("labels", labels);
        }


        if (issueFields.customFields() != null) {
            for (Long custId : issueFields.customFields().keySet()) {
                String[] values = issueFields.customFields().get(custId);
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("values", values);
                FieldInput parentField = new FieldInput(String.valueOf(custId), new ComplexIssueInputFieldValue(valueMap));
                issueInputBuilder.setFieldInput(parentField);
            }
        }

        if (issueFields.versions() != null) {
            List<String> affectVersions = issueFields.versions().stream().map(ResourceId::id).collect(Collectors.toList());
            if (affectVersions != null && affectVersions.size() > 0) {
                List<String> versionList = new ArrayList<>();
                project.getVersions().forEach(version -> {
                    if (affectVersions.contains(String.valueOf(version.getId()))) {
                        versionList.add(version.getName());
                    }
                });
                issueInputBuilder.setAffectedVersionsNames(versionList);
            }
        }

        if (issueFields.fixVersions() != null) {
            List<String> fixVersions = issueFields.fixVersions().stream().map(ResourceId::id).collect(Collectors.toList());
            if (fixVersions != null && fixVersions.size() > 0) {
                List<String> versionList = new ArrayList<>();
                project.getVersions().forEach(version -> {
                    if (fixVersions.contains(String.valueOf(version.getId()))) {
                        versionList.add(version.getName());
                    }
                });
                issueInputBuilder.setFixVersionsNames(versionList);
            }
        }

        if (issueFields.getWorklog() != null) {
            issueInputBuilder.setFieldValue("worklog", issueFields.getWorklog());
        }

        if (issueFields.getTimetracking() != null) {
            Map<String, Object> timeTracking = new HashMap<>();
            timeTracking.put("originalEstimate", issueFields.getTimetracking().getOriginalEstimate());
            issueInputBuilder.setFieldValue("timetracking", new ComplexIssueInputFieldValue(timeTracking));
        }

        if (issueFields.getDuedate() != null) {
            DateTimeFormatter inFormatter = DateTimeFormat.forPattern("d/MMM/yy");
            DateTime dateTime = inFormatter.parseDateTime(issueFields.getDuedate());
            issueInputBuilder.setDueDate(dateTime.toDateTime(DateTimeZone.UTC));
        }

        ResourceId parentId = issueFields.parent();
        if (parentId != null) {
            CaptureIssue issue = getCaptureIssue(parentId.id());
            Map<String, Object> parent = new HashMap<>();
            parent.put("key", issue.getKey());
            FieldInput parentField = new FieldInput("parent", new ComplexIssueInputFieldValue(parent));
            issueInputBuilder.setFieldInput(parentField);
        }

        return issueInputBuilder.build();
    }

    private String buildIssueCacheKey(String issueIdOrKey) {
        return ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + issueIdOrKey;
    }


}
