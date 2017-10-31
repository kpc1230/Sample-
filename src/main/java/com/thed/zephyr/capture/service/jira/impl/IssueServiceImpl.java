package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.IssueRaisedBean;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.*;
import com.thed.zephyr.capture.model.util.SessionDtoSearchList;
import com.thed.zephyr.capture.model.view.SessionDto;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueLinkTypeService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.issue.IssueCreateRequest;
import com.thed.zephyr.capture.service.jira.issue.IssueFields;
import com.thed.zephyr.capture.service.jira.issue.IssueLinks;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private IssueLinkTypeService issueLinkTypeService;


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
        SessionDto raisedDuringSessionDto = sessionService.getSessionRaisedDuring(host.getUserKey().get(), ctdId, issue.getId());
        SessionDtoSearchList sessionByRelatedIssueId = sessionService.getSessionByRelatedIssueId(host.getUserKey().get(), ctdId, issue.getProjectId(), issue.getId());
        TestSectionResponse testSectionResponse = new TestSectionResponse();
        testSectionResponse.setSessions(sessionByRelatedIssueId.getContent());
        testSectionResponse.setRaisedDuring(raisedDuringSessionDto);
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
    public CaptureIssue createIssue(HttpServletRequest request, String testSessionId, IssueCreateRequest createRequest) throws CaptureValidationException, RestClientException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        IssueFields issueFields = createRequest.fields();
        IssueInput issueInput = createIssueInput(issueFields, request);
        BasicIssue basicIssue = null;
        try {
            basicIssue = postJiraRestClient.getIssueClient().createIssue(issueInput).claim();
        } catch (RestClientException exception) {
            log.error("Error during creating issue in Jira.", exception);
            throw exception;
        }
        Issue issue = getIssueObject(basicIssue.getKey());
        //link the issues
        if (issueFields.getIssuelinks() != null) {
            IssueLinks issueLinks = issueFields.getIssuelinks();
            if(issueLinks.getIssues()!=null&&issueLinks.getIssues().length>0) {
                List<IssuelinksType> linkTypes = issueLinkTypeService.getIssuelinksType(host);
                IssuelinksType linkType = null;

                try {
                    linkType = linkTypes.stream().filter(o -> o.getInward().equals(issueLinks.getLinktype()) || o.getOutward().equals(issueLinks.getLinktype())).findFirst().get();
                } catch (NoSuchElementException exp) {
                    log.error("The Issue Types not exist", exp.getMessage());
                }
                if (linkType != null) {
                    String linkTypeToSend = linkType.getName();
                    if (linkType.getInward().equalsIgnoreCase(issueLinks.getLinktype())) {
                        Arrays.asList(issueLinks.getIssues()).forEach(s -> {
                            LinkIssuesInput linkIssuesInput = new LinkIssuesInput(s, issue.getKey(), linkTypeToSend);
                            postJiraRestClient.getIssueClient().linkIssue(linkIssuesInput);
                        });

                    } else {
                        if (linkType.getOutward().equalsIgnoreCase(issueLinks.getLinktype())) {
                            Arrays.asList(issueLinks.getIssues()).forEach(s -> {
                                LinkIssuesInput linkIssuesInput = new LinkIssuesInput(issue.getKey(), s, linkTypeToSend);
                                postJiraRestClient.getIssueClient().linkIssue(linkIssuesInput);
                            });
                        }
                    }

                }
            }

        }
        //Set Context Params
        captureContextIssueFieldsService.populateContextFields(request, issue, createRequest.getContext());
        CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
        		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(), basicIssue.getKey(), basicIssue.getId(), CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName(), resolution,null);
        if (StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            if (session != null) {
                List<Long> issueIds = new ArrayList<>();
                issueIds.add(issue.getId());
                captureContextIssueFieldsService.addRaisedInIssueField(host.getUserKey().get(), issueIds, session);
            }
        }
        return captureIssue;
    }

   @Override
    public void addComment(String issueKey, String comment) throws JSONException {
        Issue issue = getIssueObject(issueKey);
        JSONObject jsonObject = new JSONObject(comment);
        postJiraRestClient.getIssueClient().addComment(issue.getCommentsUri(),Comment.valueOf(jsonObject.get("comment").toString())).claim();
    }

    @Override
    public void linkIssues(List<SessionServiceImpl.CompleteSessionIssueLink> issueLinks , AtlassianHostUser hostUser) {

        List<IssuelinksType> linkTypes = issueLinkTypeService.getIssuelinksType(hostUser);
        IssuelinksType linkType = null;
        String linkTypeToSend = null;
        try {
            linkType = linkTypes.stream().filter(o -> o.getName().equals(ApplicationConstants.CAPTURE_TESTING_ISSUE_LINKTYPE)).findFirst().get();
        }catch (NoSuchElementException exp){
            log.error("The Issue Types not exist adding one", exp.getMessage());
        }
        if(linkType!=null){
            linkTypeToSend = linkType.getName();
        }else {
            String outgoingName = i18n.getMessage("bonfire.issue.link.outgoing");
            String incomingName = i18n.getMessage("bonfire.issue.link.incoming");
            IssuelinksType createReq = new IssuelinksType(null, null, ApplicationConstants.CAPTURE_TESTING_ISSUE_LINKTYPE, incomingName, outgoingName);
            IssuelinksType resp1 =  issueLinkTypeService.createIssuelinkType(createReq,hostUser);
           linkTypeToSend = resp1.getName();
        }
        String linkTypeStr = new String(linkTypeToSend);
        String url = hostUser.getHost().getBaseUrl()+JiraConstants.REST_API_ISSUE_LINK;
        issueLinks.forEach(issueLink -> {
            log.debug("linkIssues Staretd for Raised : {}, Related : {} ",issueLink.getRaised().getKey(),issueLink.getRelated().getKey());
            JSONObject reqJson = new JSONObject();
            ResponseEntity<JsonNode> resp =null;
        try {
            reqJson.put("type", (new JSONObject()).put("name", linkTypeStr));
            reqJson.put("inwardIssue", (new JSONObject()).put("key", issueLink.getRaised().getKey()));
            reqJson.put("outwardIssue", (new JSONObject()).put("key", issueLink.getRelated().getKey()));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestUpdate = new HttpEntity<>(reqJson.toString(),httpHeaders);
            resp = atlassianHostRestClients.authenticatedAs(hostUser).exchange(url, HttpMethod.POST,requestUpdate,JsonNode.class);
        }catch (Exception exp){
          log.error("Error in linkIssue : "+exp.getMessage(),exp);
        }

        });
    }

    @Override
    public void addTimeTrakingToIssue(Issue issue,DateTime sessionCreationOn, Long durationInMilliSeconds,String comment, AtlassianHostUser hostUser) {
        try {
            Long durationinMinitus = TimeUnit.MILLISECONDS.toMinutes(durationInMilliSeconds);
            int min = java.lang.Math.toIntExact(durationinMinitus);
            if (min > 0) {
                WorklogInput worklogInput = WorklogInput.create(issue.getSelf(), comment, sessionCreationOn, min);
                postJiraRestClient.getIssueClient().addWorklog(issue.getWorklogUri(), worklogInput).claim();
            } else {
                log.warn("Cannot log the time if it is zero min : {}", min);
            }
        } catch (Exception exp) {
            log.error("Exception while getting the issue from JIRA." + exp.getMessage(), exp);
        }
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

    private IssueInput createIssueInput(IssueFields issueFields, HttpServletRequest request) throws CaptureValidationException {
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


        /*if (issueFields.customFields() != null) {
            for (Long custId : issueFields.customFields().keySet()) {
                String[] values = issueFields.customFields().get(custId);
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("values", values);
                FieldInput parentField = new FieldInput(String.valueOf(custId), new ComplexIssueInputFieldValue(valueMap));
                issueInputBuilder.setFieldInput(parentField);
            }
        }*/

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
            try {
                DateTimeFormatter inFormatter = DateTimeFormat.forPattern("dd/MMM/yy");
                DateTime dateTime = inFormatter.parseDateTime(issueFields.getDuedate());
                dateTime.withZone(DateTimeZone.UTC);
                issueInputBuilder.setDueDate(dateTime.toDateTime());
            } catch (Exception ex) {
                try {
                    DateTimeFormatter inFormatter = DateTimeFormat.forPattern("dd/MMM/yy HH:mm a");
                    DateTime dateTime = inFormatter.parseDateTime(issueFields.getDuedate());
                    dateTime.withZone(DateTimeZone.UTC);
                    issueInputBuilder.setDueDate(dateTime.toDateTime());
                } catch (Exception ex2) {
                    log.error("Error while formatting the date ", ex2);
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy");
                    String strDate = formatter.format(date);
                    throw new CaptureValidationException(null, "duedate", i18n.getMessage("issue.create.duedate.validation.error", new Object[]{"dd/MMM/yy HH:mm a", strDate}));

                }
            }
        }
        ResourceId parentId = issueFields.parent();
        if (parentId != null) {
            CaptureIssue issue = getCaptureIssue(parentId.id());
            Map<String, Object> parent = new HashMap<>();
            parent.put("key", issue.getKey());
            FieldInput parentField = new FieldInput("parent", new ComplexIssueInputFieldValue(parent));
            issueInputBuilder.setFieldInput(parentField);
        }
        if (issueFields.getFields() != null && !issueFields.getFields().isEmpty()){
            configCustomFields(issueInputBuilder, issueFields);
        }

        return issueInputBuilder.build();
    }

    private void configCustomFields(IssueInputBuilder issueInputBuilder, IssueFields issueFields){
        String url = "/rest/api/2/issue/createmeta?expand=projects.issuetypes.fields";
        ResponseEntity<JsonNode> forEntity = atlassianHostRestClients.authenticatedAsAddon().getForEntity(url, JsonNode.class);
        JsonNode body = forEntity.getBody();
        ArrayNode projects = (ArrayNode)body.get("projects");
        JsonNode project = null;
        for(JsonNode projectJson:projects){
            if(StringUtils.equals(projectJson.get("id").asText(), issueFields.project.id())){
                project = projectJson;
                break;
            }
        }
        if(project == null){
            return;
        }
        JsonNode issueType = null;
        ArrayNode issueTypes = (ArrayNode)project.get("issuetypes");
        for (JsonNode issueTypeJson:issueTypes){
            if(StringUtils.equals(issueTypeJson.get("id").asText(), issueFields.issueType.id())){
                issueType = issueTypeJson;
                break;
            }
        }
        if (issueType == null){
            return;
        }
        JsonNode fields = issueType.get("fields");

        for (Map.Entry<String, String[]> entry:issueFields.getFields().entrySet()){
            try {
                String fieldId = entry.getKey();
                String[] fieldValue = entry.getValue();
                String fieldType = fields.get(fieldId).get("schema").get("type").asText();
                String items = fields.get(fieldId).get("schema").get("items") != null?fields.get(fieldId).get("schema").get("items").asText():"";
                if (StringUtils.equals(fieldType, "string")){
                    issueInputBuilder.setFieldValue(fieldId, fieldValue[0]);
                } else if(StringUtils.equals(fieldType, "option") || StringUtils.equals(fieldType, "version")){
                   if(fieldValue[0] != null && fieldValue[0].length() > 0) {
                	   Map<String, Object> optionValue = new HashMap<>();
                       optionValue.put("id", fieldValue[0]);
                       issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(optionValue));
                   }
                } else if(StringUtils.equals(fieldType, "any") && StringUtils.isNotBlank(fieldValue[0])){
                    issueInputBuilder.setFieldValue(fieldId, fieldValue[0]);
                } else if(StringUtils.equals(fieldType, "array") && (StringUtils.equals(items, "option") || StringUtils.equals(items, "version"))){
                    List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                    List<String> values = Arrays.asList(fieldValue);
                    values.stream().forEach((value) -> {
                        Map<String, Object> complexValue = new TreeMap<>();
                        complexValue.put("id", value);
                        checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                    });
                    issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                } else if(StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "group")){
                    List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                    List<String> values = Arrays.asList(fieldValue);
                    values.stream().forEach((value) -> {
                        Map<String, Object> complexValue = new TreeMap<>();
                        complexValue.put("name", value);
                        checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                    });
                    issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                } else if(StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "user")){
                    List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                    List<String> values = Arrays.asList(fieldValue);
                    values.stream().forEach((value) -> {
                    	for(String name :  value.trim().split(",")) {
                    		if(!StringUtils.isEmpty(name)) {
                    			Map<String, Object> complexValue = new TreeMap<>();
                                complexValue.put("name", name);
                                checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                    		}
                    	}                        
                    });
                    issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                } else if(StringUtils.equals(fieldType, "date") && StringUtils.isNotBlank(fieldValue[0])){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy");
                    Date date = sdf.parse(fieldValue[0]);
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateStr = sdf.format(date);
                    issueInputBuilder.setFieldValue(fieldId, dateStr);
                } else if(StringUtils.equals(fieldType, "datetime")){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy hh:mm a");
                    Date date = sdf.parse(fieldValue[0]);
                    sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String dateStr = sdf.format(date);
                    issueInputBuilder.setFieldValue(fieldId, dateStr);
                } else if (StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "string")){
                    List<String> values = Arrays.asList(fieldValue);
                    issueInputBuilder.setFieldValue(fieldId, values);
                } else if (StringUtils.equals(fieldType, "number")){
                    String numberStr = fieldValue[0];
                    if(StringUtils.isNotBlank(numberStr)){
                        Double number = Double.valueOf(numberStr);
                        issueInputBuilder.setFieldValue(fieldId, number);
                    }
                } else if(StringUtils.equals(fieldType, "user") || StringUtils.equals(fieldType, "group")){
                    Map<String, Object> complexValue = new TreeMap<>();
                    complexValue.put("name", fieldValue[0]);
                    issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                } else if (StringUtils.equals(fieldType, "option-with-child") && StringUtils.isNotBlank(fieldValue[0]) && StringUtils.isNotBlank(fieldValue[1])){
                    Map<String, Object> complexValue = new TreeMap<>();
                    complexValue.put("id",fieldValue[0]);
                    Map<String, Object> childValue = new TreeMap<>();
                    childValue.put("id",fieldValue[1]);
                    complexValue.put("child", new ComplexIssueInputFieldValue(childValue));
                    issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                }
            } catch (Exception exception) {
                log.error("Error during config custom field for Jira issue create request customFieldId:{} issueType:{}", entry.getKey(), issueFields.issueType().id(), exception);
            }
        }
    }

    private String buildIssueCacheKey(String issueIdOrKey) {
        return ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + issueIdOrKey;
    }
}
