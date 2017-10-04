package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.json.IssueJsonParser;
import com.atlassian.util.concurrent.Promise;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureEnvironment;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureResolution;
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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
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
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;


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
                    CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
                    		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
                    return new CaptureIssue(issue.getSelf(),
                            issue.getKey(), issue.getId(),
                            CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName(), resolution,null);
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
                	CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
                    		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
                    captureIssues.add(new CaptureIssue(issue.getSelf(),
                            issue.getKey(), issue.getId(),
                            CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName(), resolution,null));
                });
        return captureIssues;
    }

    @Override
    public TestSectionResponse getIssueSessionDetails(CaptureIssue issue) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String ctdId = CaptureUtil.getCurrentCtId(dynamoDBAcHostRepository);
        Session raisedDuring = sessionService.getSessionRaisedDuring(ctdId, issue.getId());
        SessionDtoSearchList sessionByRelatedIssueId = sessionService.getSessionByRelatedIssueId(host.getUserKey().get(), ctdId, issue.getProjectId(), issue.getId());
        TestSectionResponse testSectionResponse = new TestSectionResponse();
        testSectionResponse.setSessions(sessionByRelatedIssueId.getContent());
        testSectionResponse.setRaisedDuring(raisedDuring);
        CaptureEnvironment captureEnvironment = new CaptureEnvironment();
        if (issue.getProperties() != null) {
            captureEnvironment.setUserAgent(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME,"-"));
            captureEnvironment.setBrowser(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME,null));
            captureEnvironment.setjQueryVersion(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME,"-"));
            captureEnvironment.setDocumentMode(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE,"-"));
            captureEnvironment.setScreenResolution(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME,"-"));
            captureEnvironment.setUrl(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME,"-"));
            captureEnvironment.setOperatingSystem(issue.getProperties().getOrDefault(CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME,null));
        }
        captureEnvironment.setBrowserIcon(CaptureUtil.getBrowserIcon(captureEnvironment.getBrowser()));
        captureEnvironment.setOsIcon(CaptureUtil.getOSIcon(captureEnvironment.getOperatingSystem()));
        testSectionResponse.setCaptureEnvironment(captureEnvironment);

        TestingStatus testingStatus = new TestingStatus();
        testingStatus.setTotalCount(sessionByRelatedIssueId.getContent() != null ? sessionByRelatedIssueId.getContent().size() : new Double(0));
        AtomicInteger startedCount = new AtomicInteger();
        AtomicInteger completedCount = new AtomicInteger();
        AtomicInteger inProgressCount = new AtomicInteger();
        AtomicInteger notStartedCount = new AtomicInteger();

        boolean emptyList =  sessionByRelatedIssueId.getContent()!=null&&sessionByRelatedIssueId.getContent().isEmpty() ? true :false;

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
            } else if ((notStartedCount.get() != 0 && completedCount.get() == 0) || emptyList) {
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
        testingStatus.setInProgressCount(new Double(inProgressCount.get()));
        testingStatus.setNotStartedCount(new Double(notStartedCount.get()));

        Double notStartedPercent = 0.0, inProgressPercent = 0.0, completePercent = 0.0;
        if (testingStatus.getTotalCount().intValue() != 0) {
            notStartedPercent = Math.floor(((notStartedCount.get()* 100) / testingStatus.getTotalCount().intValue()));
            inProgressPercent = Math.floor(((inProgressCount.get()* 100) / testingStatus.getTotalCount().intValue()));
            completePercent = Math.floor(((completedCount.get()* 100) / testingStatus.getTotalCount().intValue()));
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
        CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
        		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(), basicIssue.getKey(), basicIssue.getId(), CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName(), resolution,null);
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

    @Override
    public void addComment(String issueKey, String comment) throws JSONException {
        Issue issue = getIssueObject(issueKey);
        JSONObject jsonObject = new JSONObject(comment);
        postJiraRestClient.getIssueClient().addComment(issue.getCommentsUri(),Comment.valueOf(jsonObject.get("comment").toString())).claim();
    }

    public CaptureIssue searchPropertiesByJql(String issueKey, String allProperties) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        CaptureIssue captureIssue = null;
        try {
            captureIssue = tenantAwareCache.getOrElse(acHostModel, buildIssueCacheKey(issueKey), new Callable<CaptureIssue>() {
                @Override
                public CaptureIssue call() throws Exception {
                    CaptureIssue finalCaptureIssue = null;
                    URI targetUrl= UriComponentsBuilder.fromUriString(acHostModel.getBaseUrl())
                            .path(JiraConstants.REST_API_SEARCH)
                            .queryParam("jql","issue="+issueKey+"&properties="+allProperties)
                            .build()
                            .toUri();

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    String resourceUrl = targetUrl.toString();
                    HttpEntity<String> requestUpdate = new HttpEntity<>(httpHeaders);
                    ResponseEntity<JsonNode> jsonNodeResponseEntity = atlassianHostRestClients.authenticatedAsAddon().exchange(resourceUrl, HttpMethod.GET,requestUpdate,JsonNode.class);
                    JSONObject jsonObject = new JSONObject(jsonNodeResponseEntity.getBody().toString());
                    Object issueObject = jsonObject.get("issues");
                    if(issueObject != null) {
                        JSONArray jsonArray = (JSONArray) issueObject;
                        if(jsonArray.length() > 0) {
                            JSONObject issue = jsonArray.getJSONObject(0);
                            JSONObject fields = issue.getJSONObject("fields");
                            JSONObject issuetype = fields != null && fields.has("issuetype") && !fields.isNull("issuetype") ? fields.getJSONObject("issuetype") : null;
                            JSONObject resolution = fields != null && fields.has("resolution") && !fields.isNull("resolution") ? fields.getJSONObject("resolution") : null;
                            JSONObject properties = issue.has("properties") && !issue.isNull("properties") ? issue.getJSONObject("properties") : null;
                            String captureBrowserName = properties.has(CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase()) ? properties.getJSONObject(CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase()).getString("content") : "-";
                            String captureOS = properties.has(CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase()) ? properties.getJSONObject(CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase()).getString("content") : "-";
                            String captureDocument = properties.has(CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase()) ? properties.getJSONObject(CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase()).getString("content") : "-";
                            String captureUserAgent = properties.has(CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase()) ? properties.getJSONObject(CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase()).getString("content") : "-";
                            String captureScreenRes = properties.has(CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase()) ? properties.getJSONObject(CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase()).getString("content") : "-";

                            //Unfortunately Jira only allows max of 5 properties to be fetched as query
                            StringBuilder basePath = new StringBuilder();
                            basePath.append(JiraConstants.REST_API_BASE_ISSUE).append("/").append(issueKey).append("/properties/");
                            String urlPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ", "_");
                            String captureJiraURL = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), urlPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ", "_"));

                            String jQueryVersionPath = basePath.toString() + CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ", "_");
                            String capturejQuery = captureContextIssueFieldsService.getContextFields(host.getHost().getBaseUrl(), jQueryVersionPath, CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ", "_"));

                            Map<String,String> propertiesMap = new HashMap<>();
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME,captureBrowserName);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME,captureJiraURL);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME,captureOS);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE,captureDocument);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME,capturejQuery);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME,captureUserAgent);
                            propertiesMap.put(CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME,captureScreenRes);

                            CaptureResolution captureResolution = null;
                            if(resolution != null) {
                                captureResolution = new CaptureResolution(resolution.getLong("id"),resolution.getString("name"),new URI(resolution.getString("self")));
                            }
                            finalCaptureIssue = new CaptureIssue(new URI(issue.getString("self")), issue.getString("key"), issue.getLong("id"),
                                    issuetype != null ? issuetype.getString("iconUrl") : "", fields.getString("summary"), fields.getJSONObject("project").getLong("id"),
                                    fields.getJSONObject("project").getString("key"), fields.getJSONObject("reporter") != null ? fields.getJSONObject("reporter").getString("name") : "",
                                    captureResolution, propertiesMap);
                        }
                    }
                    return finalCaptureIssue;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.ISSUE_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exp) {
            log.error("Exception while getting the issue from JIRA." + exp.getMessage(), exp);
        }
        log.debug("ISSUE: --> {}", captureIssue != null ? captureIssue.getSummary() : "-");
        return captureIssue;
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
