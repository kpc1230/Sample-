package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.ExpandableProperty;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
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
import com.thed.zephyr.capture.service.jira.*;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.thed.zephyr.capture.util.JiraConstants.REST_API_ISSUE;
import static com.thed.zephyr.capture.util.JiraConstants.REST_API_PROJECT;

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
    @Autowired
    private CJiraRestClientFactory cJiraRestClientFactory;
    @Autowired
    private MetadataService metadataService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;

    @Override
    public Issue getIssueObject(String issueIdOrKey) {
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        RestTemplate restTemplate = atlassianHostRestClients.authenticatedAsAddon();
        if(hostUser != null){
            restTemplate = atlassianHostRestClients.authenticatedAs(hostUser);
        }
        try{
        ResponseEntity<JsonNode> jsonNodeRE = restTemplate.getForEntity(REST_API_ISSUE+"/"+issueIdOrKey, JsonNode.class);
        if(jsonNodeRE != null && jsonNodeRE.getStatusCodeValue()==200) {
            JsonNode issueJsonBody = jsonNodeRE.getBody();
            JsonNode issueJson = issueJsonBody.get("fields");
            JsonNode statusJson = issueJson != null && issueJson.has("status") ? issueJson.get("status") : null;
            Status status = CaptureUtil.getStatus(statusJson);
            JsonNode itNode = issueJson.get("issuetype");
            IssueType issueType = CaptureUtil.getIssueType(itNode);

            JsonNode pNode = issueJson.get("project");
            Long pId = pNode.has("id") ? pNode.get("id").asLong() : null;
            URI pSelf = null;
            try {
                pSelf = pNode.has("self") ? new URI(pNode.get("self").asText()) : new URI("");
            } catch (Exception ex) {
            }
            String pName = pNode.has("name") ? pNode.get("name").asText() : "";
            String pKey = pNode.has("key") ? pNode.get("key").asText() : null;
            BasicProject project = new BasicProject(pSelf, pKey, pId, pName);

            URI transitionsUri = new URI("");
            List<String> expandos = Lists.newArrayList();
            JsonNode expandosJson = issueJsonBody.get("expand");
            if (expandosJson != null && expandosJson.size() > 0) {
                expandosJson.forEach(exNode -> {
                    expandos.add(exNode.asText());
                });
            }
            List<BasicComponent> components = CaptureUtil.getComponents(issueJson.get("components"));

            String summary = issueJson.has("summary") ? issueJson.get("summary").asText() : "";
            String description = issueJson.has("description") ? issueJson.get("description").asText() : "";
            String key = issueJsonBody.has("key") ? issueJsonBody.get("key").asText() : "";
            Long id = issueJsonBody.has("id") ? issueJsonBody.get("id").asLong() : null;
            URI self = null;
            try {
                self = issueJsonBody.has("self") ? new URI(issueJsonBody.get("self").asText()) : new URI("");
            } catch (Exception ex) {
            }
            JsonNode reporterJson = issueJson.get("reporter");
            URI rSelf = null;
            try {
                rSelf = reporterJson.has("self") ? new URI(reporterJson.get("self").asText()) : new URI("");
            } catch (Exception ex) {
            }
            String rName = reporterJson.has("name") ? reporterJson.get("name").asText() : "";
            String rDisplayName = reporterJson.has("displayName") ? reporterJson.get("displayName").asText() : "";
            String rEmailAddress = reporterJson.has("emailAddress") ? reporterJson.get("emailAddress").asText() : "";
            JsonNode rGroupsNode = reporterJson.get("groups");
            List<String> rGroups = Lists.newArrayList();
            rGroups.add("jira-software-users");
            ExpandableProperty expandableProperty = new ExpandableProperty(rGroups);
            if (rGroupsNode != null && rGroupsNode.size() > 0) {
                rGroupsNode.forEach(rNode -> {
                    rGroups.add(rNode.asText());
                });
                expandableProperty = new ExpandableProperty(rGroups);
            }

            Map<String, URI> rAvatarUris = new HashMap<>();
            rAvatarUris.put("16x16",new URI("xxx"));
            rAvatarUris.put("24x24",new URI("xxx"));
            rAvatarUris.put("32x32",new URI("xxx"));
            rAvatarUris.put("48x48",new URI("xxx"));
            String rTimezone = reporterJson.has("timeZone")? reporterJson.get("timeZone").asText(): "";;
            User reporter = new User(rSelf,rName,rDisplayName,rEmailAddress,expandableProperty,rAvatarUris,rTimezone);

            JsonNode assigneeJson = issueJson.has("assignee") ? issueJson.get("assignee"): null;
            User assignee = null;
            if(assigneeJson != null && !assigneeJson.isNull()) {
                URI aSelf = null;
                try {
                    aSelf = assigneeJson.has("self") ? new URI(assigneeJson.get("self").asText()) : new URI("");
                } catch (Exception ex) {
                }
                String aName = assigneeJson.has("name") ? assigneeJson.get("name").asText() : "";
                String aDisplayName = assigneeJson.has("displayName") ? assigneeJson.get("displayName").asText() : "";
                String aEmailAddress = assigneeJson.has("emailAddress") ? assigneeJson.get("emailAddress").asText() : "";
                JsonNode aGroupsNode = assigneeJson.get("groups");
                List<String> aGroups = Lists.newArrayList();
                aGroups.add("jira-software-users");
                ExpandableProperty expandablePropertyA = new ExpandableProperty(aGroups);
                if(aGroupsNode != null && aGroupsNode.size()>0) {
                    aGroupsNode.forEach(aNode -> {
                        aGroups.add(aNode.asText());
                    });
                    expandablePropertyA = new ExpandableProperty(aGroups);
                }
                Map<String, URI> aAvatarUris = new HashMap<>();
                aAvatarUris.put("16x16",new URI("xxx"));
                aAvatarUris.put("24x24",new URI("xxx"));
                aAvatarUris.put("32x32",new URI("xxx"));
                aAvatarUris.put("48x48",new URI("xxx"));
                String aTimezone = assigneeJson.has("timeZone") ? assigneeJson.get("timeZone").asText() : "";
                assignee = new User(aSelf, aName, aDisplayName, aEmailAddress, expandablePropertyA, aAvatarUris, aTimezone);
            }
            JsonNode resolutionNode = issueJson.has("resolution") ? issueJson.get("resolution"):null;
            Resolution resolution = null;
            if(resolutionNode != null && !resolutionNode.isNull()) {
                Long resId = resolutionNode.has("id") ? resolutionNode.get("id").asLong() : null;
                URI resSelf = null;
                try {
                    resSelf = resolutionNode.has("self") ? new URI(resolutionNode.get("self").asText()) : new URI("");
                } catch (Exception ex) {
                }
                String resName = resolutionNode.has("name") ? resolutionNode.get("name").asText() : "";
                String resDescription = resolutionNode.has("description") ? resolutionNode.get("description").asText() : null;
                  resolution = new Resolution(resSelf, resId, resName, resDescription);
            }

            DateTime creationDate = issueJson.has("creationDate")? DateTime.parse(issueJson.get("creationDate").asText()): null;
            DateTime updateDate = issueJson.has("updateDate")? DateTime.parse(issueJson.get("updateDate").asText()): null;
            DateTime dueDate = issueJson.has("dueDate")? DateTime.parse(issueJson.get("dueDate").asText()): null;

            JsonNode priorityNode = issueJson.get("priority");
            BasicPriority priority = null;
            if(priorityNode != null) {
                Long priId = priorityNode.has("id") ? priorityNode.get("id").asLong() : null;
                URI priSelf = priorityNode.has("self") ? new URI(priorityNode.get("self").asText()) : new URI("");
                String priName = priorityNode.has("name") ? priorityNode.get("name").asText() : "";
                  priority = new BasicPriority(priSelf, priId, priName);
            }
            JsonNode votesNode = issueJson.has("votes")?issueJson.get("votes"): null;
            BasicVotes votes = null;
            if(votesNode != null) {
                URI votSelf = votesNode.has("self") ? new URI(votesNode.get("self").asText()) : new URI("");
                Integer votInt = votesNode.has("notes") ? votesNode.get("notes").asInt() : 0;
                Boolean votHasVoted = votesNode.has("hasVoted") ? votesNode.get("hasVoted").asBoolean() : false;
                  votes = new BasicVotes(votSelf, votInt, votHasVoted);
            }

            JsonNode issueFieldsNode = issueJson.has("issueFields") ? issueJson.get("issueFields"): null;
            List<IssueField> issueFields = Lists.newArrayList() ;
            if(issueFieldsNode != null && issueFieldsNode.size()>0) {
                issueFieldsNode.forEach(ifNode -> {
                    String ifId = ifNode.has("id") ? ifNode.get("id").asText() : null;
                    String ifName = ifNode.has("name") ? ifNode.get("name").asText() : "";
                    String ifType = ifNode.has("type") ? ifNode.get("type").asText() : "";
                    String ifValue = ifNode.has("value") ? ifNode.get("value").toString() : "";
                    IssueField issueField = new IssueField(ifId, ifName, ifType, ifValue);
                    issueFields.add(issueField);
                });
            }

            List<Version> fixVersions = CaptureUtil.getVersions(issueJson.get("fixVersions"));
            Collection<Version> affectedVersions = CaptureUtil.getVersions(issueJson.get("affectedVersions"));

            List<Comment> comments = Lists.newArrayList();
            JsonNode commentsJson = issueJson.has("comments") ? issueJson.get("comments"): null;
            if(commentsJson != null && commentsJson.size() > 0){
                commentsJson.forEach(comNode->{
                    URI comSelf = null;
                    try {
                        comSelf = comNode.has("self")? new URI(comNode.get("self").asText()): new URI("");
                    }catch (Exception ex){ }
                    Long comId = comNode.has("id")? comNode.get("id").asLong(): null;
                    BasicUser comAuthor = CaptureUtil.getAuthor(comNode.get("author"));
                    BasicUser comUpdateAuthor = CaptureUtil.getAuthor(comNode.get("updateAuthor"));;
                    DateTime comCreationDate = comNode.has("creationDate")? DateTime.parse(comNode.get("creationDate").asText()): null;
                    DateTime comUpdateDate = comNode.has("updateDate")? DateTime.parse(comNode.get("updateDate").asText()): null;
                    String comBody = comNode.has("body")? comNode.get("body").asText(): null;
                    Visibility comVisibility = null;
                    Comment comment = new Comment(comSelf, comBody, comAuthor, comUpdateAuthor, comCreationDate,
                            comUpdateDate, comVisibility, comId);
                    comments.add(comment);
                });
            }

            List<IssueLink> issueLinks = Lists.newArrayList();
            JsonNode issueLinksNode = issueJson.has("issueLinks") ? issueJson.get("issueLinks") : null;
            if(issueFieldsNode != null && issueLinks.size() > 0){
                issueLinksNode.forEach(ilNode ->{
                    String targetIssueKey = ilNode.has("targetIssueKey") ? ilNode.get("targetIssueKey").asText(): null;
                    URI targetIssueUri = null;
                    try {
                        targetIssueUri = ilNode.has("targetIssueKey")? new URI(ilNode.get("targetIssueKey").asText()): new URI("");
                    }catch (Exception ex){ }
                    String bound = ilNode.has("issueLinkType") ? ilNode.get("issueLinkType").asText() : null;
                    IssueLinkType.Direction direction = (bound != null && bound.equalsIgnoreCase("OUTBOUND") ? IssueLinkType.Direction.OUTBOUND: IssueLinkType.Direction.INBOUND);
                    IssueLinkType issueLinkType = new IssueLinkType(bound, bound, direction);
                    IssueLink issueLink = new IssueLink(targetIssueKey, targetIssueUri, issueLinkType);
                    issueLinks.add(issueLink);
                });
            }


            List<Attachment> attachments = Lists.newArrayList();
            JsonNode attachmentsNode = issueJson.has("attachments") ? issueJson.get("attachments") : null;
            if(attachmentsNode != null && attachmentsNode.size()>0){
                attachmentsNode.forEach(attNode->{

                    String attFilename = attNode.has("filename") ? attNode.get("filename").asText(): null;
                    URI attSelf = null;
                    try {
                        attSelf = attNode.has("self")? new URI(attNode.get("self").asText()): new URI("");
                    }catch (Exception ex){ }
                    BasicUser attAuthor = CaptureUtil.getAuthor(attNode.get("author"));
                    DateTime attCreationDate = attNode.has("creationDate")? DateTime.parse(attNode.get("creationDate").asText()): null;
                    int attSize = attNode.has("size") ? attNode.get("size").asInt(): 0;
                    String attMimeType = attNode.has("mimeType") ? attNode.get("mimeType").asText(): "";
                    URI attContentUri = null;
                    try {
                        attContentUri = attNode.has("contentUri")? new URI(attNode.get("contentUri").asText()): new URI("");
                    }catch (Exception ex){ }
                    URI atthumbnailUri = null;
                    Attachment attachment = new Attachment(attSelf, attFilename, attAuthor, attCreationDate, attSize, attMimeType, attContentUri, atthumbnailUri);
                    attachments.add(attachment);
                });
            }
            List<Worklog> worklogs = Lists.newArrayList();
            JsonNode worklogsNode = issueJson.has("worklogs")?issueJson.get("worklogs"):null;
            if(worklogsNode != null && worklogsNode.size()>0){
                worklogsNode.forEach(wlNode->{
                    URI wlSelf = null, wlIssueUri = null;
                    try {
                        wlSelf = wlNode.has("self")? new URI(wlNode.get("self").asText()): new URI("");
                        wlIssueUri = wlNode.has("issueUri")? new URI(wlNode.get("issueUri").asText()): new URI("");
                    }catch (Exception ex){ }
                    BasicUser wlAuthor = CaptureUtil.getAuthor(wlNode.get("author"));
                    BasicUser wlUpdateAuthor = CaptureUtil.getAuthor(wlNode.get("updateAuthor"));
                    String wlComment = wlNode.has("comment")?  wlNode.get("self").asText(): null;
                    DateTime wlCreationDate = wlNode.has("creationDate")? DateTime.parse(wlNode.get("creationDate").asText()): null;
                    DateTime wlUpdateDate = wlNode.has("updateDate")? DateTime.parse(wlNode.get("updateDate").asText()): null;
                    DateTime wlStartDate = wlNode.has("startDate")? DateTime.parse(wlNode.get("startDate").asText()): null;
                    int wlMinutesSpent = wlNode.has("minutesSpent")?  wlNode.get("minutesSpent").asInt(): 0;
                    Visibility wlVisibility = null;
                    Worklog worklog = new Worklog(wlSelf, wlIssueUri, wlAuthor, wlUpdateAuthor, wlComment, wlCreationDate, wlUpdateDate, wlStartDate, wlMinutesSpent, wlVisibility);
                    worklogs.add(worklog);
                });
            }
            JsonNode watcherNode = issueJson.has("watchers") ? issueJson.get("watchers"): null;
            BasicWatchers basicWatchers = null;
            if(watcherNode != null){
                URI watSelf = watcherNode.has("self") ? new URI(watcherNode.get("self").asText()): new URI("");
                Boolean watIsWatching = watcherNode.has("watching") ? watcherNode.get("watching").asBoolean(): null;
                Integer watNumWatchers = watcherNode.has("numWatchers") ? watcherNode.get("numWatchers").asInt(): 0;
                basicWatchers = new BasicWatchers(watSelf, watIsWatching, watNumWatchers);

            }

            JsonNode timeTrackingNode = issueJson.has("timeTracking") ? issueJson.get("timeTracking"): null;
            TimeTracking timeTracking=null;
            if(timeTrackingNode != null){
                Integer originalEstimateMinutes = timeTrackingNode.has("originalEstimateMinutes") ? timeTrackingNode.get("originalEstimateMinutes").asInt(): 0;
                Integer remainingEstimateMinutes= timeTrackingNode.has("remainingEstimateMinutes") ? timeTrackingNode.get("remainingEstimateMinutes").asInt(): 0;
                Integer timeSpentMinutes = timeTrackingNode.has("timeSpentMinutes") ? timeTrackingNode.get("timeSpentMinutes").asInt(): 0;
                timeTracking = new TimeTracking(originalEstimateMinutes, remainingEstimateMinutes, timeSpentMinutes);
            }

            List<Subtask> subtasks = Lists.newArrayList();
            JsonNode subtasksNode = issueJson.has("subtasks")?issueJson.get("subtasks"):null;
            if(subtasksNode != null && subtasksNode.size() > 0){
                subtasksNode.forEach(stNode->{
                    String stIssueKey = stNode.has("issueKey") ? stNode.get("issueKey").asText(): null;
                    URI stIssueUri = null;
                    try {
                        stIssueUri = stNode.has("issueUri")? new URI(stNode.get("issueUri").asText()): new URI("");
                    }catch (Exception ex){ }
                    String stSummary = stNode.has("summary") ? stNode.get("summary").asText(): null;
                    IssueType stIssueType = CaptureUtil.getIssueType(stNode.get("issueType"));
                    Status stStatus = CaptureUtil.getStatus(stNode.get("status"));
                    Subtask subtask = new Subtask(stIssueKey,stIssueUri, stSummary, stIssueType,stStatus);
                    subtasks.add(subtask);
                });
            }

            List<ChangelogGroup> changelog = Lists.newArrayList();
            Operations operations = null;
            Set<String> labels = new HashSet<>();
            JsonNode labelsNode = issueJson.has("labels")?issueJson.get("labels"):null;
            if(labelsNode != null && labelsNode.size()>0){
                labelsNode.forEach(lNode->{
                    labels.add(lNode.asText());
                });
            }
            return new Issue(summary,self,key,id,project,issueType,
                    status,description,priority,resolution,attachments,reporter,assignee,creationDate,
                    updateDate,dueDate, affectedVersions,fixVersions,components,
                    timeTracking, issueFields, comments, transitionsUri, issueLinks, votes, worklogs,
                    basicWatchers, expandos, subtasks, changelog, operations, labels);
        }else{
            return null;
        }
        }catch (Exception e){
            log.error("error during getting issue {} {}",issueIdOrKey, e.getMessage());
        }
        return null;
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
        boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
        CaptureIssue captureIssue = null;
        try {
            captureIssue = tenantAwareCache.getOrElse(acHostModel, buildIssueCacheKey(issueIdOrKey), new Callable<CaptureIssue>() {
				@Override
                public CaptureIssue call() throws Exception {
                    Issue issue = getIssueObject(issueIdOrKey);
                    CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
                    		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
                    Long parentId = null; String parentKey = null;
                    if(issue.getIssueType().isSubtask()) {
                    	IssueField parentIssueField = issue.getField("parent");
                    	if(Objects.nonNull(parentIssueField)) {
                    		JSONObject fieldValueMap = (JSONObject)parentIssueField.getValue();
                        	if(Objects.nonNull(fieldValueMap)) {
                        		try {
                        			parentId = fieldValueMap.getLong("id");
                            		parentKey = fieldValueMap.getString("key");
                        		} catch(JSONException ex) {
                        			log.error("Error while getting the parent values for the issue ", ex);
                        		}
                        	}
                    	}
                    }
                    if(isTenantGDPRComplaint) {
                    	return new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), null,
                                CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution, null, parentId, parentKey);
                    } else {
                    	return new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(), issue.getReporter().getName(),
                                CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution, null, parentId, parentKey);
                    }
                    
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.ISSUE_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
            log.debug("ISSUE: --> {}", captureIssue.getSummary());
        } catch(RestClientException restClientException){
            if (restClientException.getStatusCode().get().equals(404)){
                log.warn("Issue wasn't found in Jira issueId:{} ctId:{}", issueIdOrKey, acHostModel.getCtId());
                return null;
            }
            log.error("Exception while getting the issue from JIRA, the null value will be returned", restClientException);
        } catch (Exception exception) {
            log.error("Exception while getting the issue from JIRA, the null value will be returned", exception);
        }
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
        boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
        SearchResult searchResultPromise =
                jiraRestClient.getSearchClient().searchJql(jql).claim();
        searchResultPromise.getIssues()
                .forEach(issue -> {
                	CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
                    		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
                	Long parentId = null; String parentKey = null;
                    if(issue.getIssueType().isSubtask()) {
                    	IssueField parentIssueField = issue.getField("parent");
                    	if(Objects.nonNull(parentIssueField)) {
                    		JSONObject fieldValueMap = (JSONObject)parentIssueField.getValue();
                        	if(Objects.nonNull(fieldValueMap)) {
                        		try {
                        			parentId = fieldValueMap.getLong("id");
                            		parentKey = fieldValueMap.getString("key");
                        		} catch(JSONException ex) {
                        			log.error("Error while getting the parent values for the issue ", ex);
                        		}
                        	}
                    	}
                    }
                    if(isTenantGDPRComplaint) {
                    	captureIssues.add(new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(),
                                null, CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution,null, parentId, parentKey));
                    } else {
                    	captureIssues.add(new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(),
                                issue.getReporter().getName(), CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution,null, parentId, parentKey));
                    }                    
                });
        return captureIssues;
    }

    @Override
    public TestSectionResponse getIssueSessionDetails(CaptureIssue issue) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String ctdId = CaptureUtil.getCurrentCtId();
        SessionDto raisedDuringSessionDto = sessionService.getSessionRaisedDuring(host.getUserKey().orElse(null), host.getUserAccountId().get(), ctdId, issue.getId());
        SessionDtoSearchList sessionByRelatedIssueId = sessionService.getSessionByRelatedIssueId(host.getUserKey().orElse(null), host.getUserAccountId().get(), ctdId, issue.getProjectId(), issue.getId());
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
        IssueInput issueInput = createIssueInput(host, issueFields, request);
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
             updateIssueLinks(issue.getKey(),issueFields.getIssuelinks(),host);
        }
        //Set Context Params
        captureContextIssueFieldsService.populateContextFields(request, issue, createRequest.getContext());
        CaptureResolution resolution = issue.getResolution() != null ? new CaptureResolution(issue.getResolution().getId(), 
        		issue.getResolution().getName(), issue.getResolution().getSelf()) : null;
        Long parentId = null; String parentKey = null;
        if(issue.getIssueType().isSubtask()) {
        	IssueField parentIssueField = issue.getField("parent");
        	if(Objects.nonNull(parentIssueField)) {
        		JSONObject fieldValueMap = (JSONObject)parentIssueField.getValue();
            	if(Objects.nonNull(fieldValueMap)) {
            		try {
            			parentId = fieldValueMap.getLong("id");
                		parentKey = fieldValueMap.getString("key");
            		} catch(JSONException ex) {
            			log.error("Error while getting the parent values for the issue ", ex);
            		}
            	}
        	}
        }
        CaptureIssue captureIssue = new CaptureIssue(basicIssue.getSelf(), basicIssue.getKey(), basicIssue.getId(), CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(),
        		null, CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution ,null, parentId, parentKey);
        if(!CaptureUtil.isTenantGDPRComplaint()) {
        	captureIssue = new CaptureIssue(basicIssue.getSelf(), basicIssue.getKey(), basicIssue.getId(), CaptureUtil.getFullIconUrl(issue, host), issue.getSummary(), issue.getProject().getId(), issue.getProject().getKey(),
            		issue.getReporter().getName(), CaptureUtil.getAccountIdFromQueryString(issue.getReporter().getSelf().getQuery()), resolution ,null, parentId, parentKey);
        }
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

    /**
     * Update Issue Link by using PostJiraRestClient
     *
     * @param issueKey
     * @param issueLinks
     * @param host
     */
    private void updateIssueLinks(String issueKey, IssueLinks issueLinks, AtlassianHostUser host) {
        final JiraRestClient postJiraRestClient  =  createPostJiraRestClient(host);
       CompletableFuture.runAsync(() -> {
            log.debug("New Thread Started in order to update issues links ");
            try{
                if(issueLinks.getIssues()!=null && issueLinks.getIssues().length>0) {
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
                                LinkIssuesInput linkIssuesInput = new LinkIssuesInput(s, issueKey, linkTypeToSend);
                                try {
                                    log.debug("Linking issues Issue Type: {} , From Issue: {} , To Issue: {} ", linkIssuesInput.getLinkType(), linkIssuesInput.getFromIssueKey(), linkIssuesInput.getToIssueKey());
                                    postJiraRestClient.getIssueClient().linkIssue(linkIssuesInput);
                                    Thread.sleep(500);
                                } catch (Exception exception) {
                                    log.error("Error during create inward link issue", exception);
                                }
                            });

                        } else {
                            if (linkType.getOutward().equalsIgnoreCase(issueLinks.getLinktype())) {
                                Arrays.asList(issueLinks.getIssues()).forEach(s -> {
                                    try {
                                        LinkIssuesInput linkIssuesInput = new LinkIssuesInput(issueKey, s, linkTypeToSend);
                                        log.debug("Linking issues Issue Type: {} , From Issue: {} , To Issue: {} ", linkIssuesInput.getLinkType(), linkIssuesInput.getFromIssueKey(), linkIssuesInput.getToIssueKey());
                                        postJiraRestClient.getIssueClient().linkIssue(linkIssuesInput);
                                        Thread.sleep(500);
                                    } catch (Exception exception) {
                                        log.error("Error during create outward link issue", exception);
                                    }
                                });
                            }
                        }

                    }
                }
            } finally {
                if(postJiraRestClient != null){
                    try {
                        postJiraRestClient.close();
                    } catch (IOException exception) {
                        log.error("Error during updateIssueLinks, can't close postJiraRestClient.", exception);
                    }
                }
            }
           log.debug("Thread Completed Issue links update");
        });
    }

    @Override
    public void addComment(String issueKey, String comment) throws JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();
        String userAccountId = hostUser.getUserAccountId().get();
        AtlassianHostUser.AtlassianHostUserBuilder atlassianHostUserBuilder = AtlassianHostUser.builder(hostUser.getHost());
        if(null != userAccountId && StringUtils.isNotEmpty(userAccountId)){
            hostUser = atlassianHostUserBuilder.withUserAccountId(userAccountId).build();
        }
        Issue issue = getIssueObject(issueKey);
        ObjectMapper mapper = new ObjectMapper();
        try {
            URI addCommentUrl = new URI(host.getHost().getBaseUrl()+JiraConstants.REST_API_COMMENT
            .replace("{issueId}",String.valueOf(issue.getId())));
            String commentStr = new ObjectMapper().readTree(comment).get("comment").asText();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JsonNode jsonNode = mapper.convertValue(Comment.valueOf(commentStr), JsonNode.class);
            HttpEntity<String> commentEntity = new HttpEntity<String>(jsonNode.toString(), headers);
            JsonNode responseNode = atlassianHostRestClients
                    .authenticatedAs(hostUser)
                    .postForObject(addCommentUrl, commentEntity, JsonNode.class);
            log.debug("Response for addComment from JIRA {} , {}", issueKey, responseNode);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error during adding comment to issue {}", issueKey);
        }
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
    public void addTimeTrakingToIssue(Issue issue, DateTime sessionCreationOn, Long durationInMilliSeconds, String comment, AtlassianHostUser hostUser) {
        try {
            Long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMilliSeconds);
            int min = java.lang.Math.toIntExact(durationInMinutes);
            if (min > 0) {
                WorklogInput worklogInput = WorklogInput.create(issue.getSelf(), comment, sessionCreationOn, min);
                postJiraRestClient.getIssueClient().addWorklog(issue.getWorklogUri(), worklogInput).claim();
            } else {
                log.warn("Cannot log the time if it is zero min : {}", min);
            }
        } catch (Exception exception) {
            log.error("Error during add tracking time into issueId:{}", issue.getId(), exception);
        }
    }

    public CaptureIssue searchPropertiesByJql(String issueKey, String allProperties) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        CaptureIssue captureIssue = null;
        try {
            captureIssue = tenantAwareCache.getOrElse(acHostModel, buildIssueCacheKeyWithProperties(issueKey), new Callable<CaptureIssue>() {
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
                            Long parentId = null; String parentKey = null;
                            if(Objects.nonNull(issuetype) && issuetype.getBoolean("subtask")) {
                            	if(Objects.nonNull(fields)) {
                            		JSONObject fieldValueMap = (JSONObject)fields.get("parent");
                                	if(Objects.nonNull(fieldValueMap)) {
                                		parentId = fieldValueMap.getLong("id");
                                		parentKey = fieldValueMap.getString("key");
                                	}
                            	}                            	
                            }
                            finalCaptureIssue = new CaptureIssue(new URI(issue.getString("self")), issue.getString("key"), issue.getLong("id"),
                                    issuetype != null ? issuetype.getString("iconUrl") : "", fields.getString("summary"), fields.getJSONObject("project").getLong("id"),
                                    fields.getJSONObject("project").getString("key"), fields.getJSONObject("reporter") != null ? fields.getJSONObject("reporter").getString("name") : "",
                                    		 fields.getJSONObject("reporter") != null ? fields.getJSONObject("reporter").getString("accountId") : "",
                                    captureResolution, propertiesMap, parentId, parentKey);
                        }
                    }
                    return finalCaptureIssue;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.ISSUE_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());
        } catch (Exception exception) {
            log.error("Exception while getting the issue from JIRA.", exception);
        }
        log.debug("ISSUE: --> {}", captureIssue != null ? captureIssue.getSummary() : "-");
        return captureIssue;
    }

    private IssueInput createIssueInput(AtlassianHostUser host, IssueFields issueFields, HttpServletRequest request) throws CaptureValidationException {
        IssueInputBuilder issueInputBuilder = new IssueInputBuilder();
        issueInputBuilder.setIssueTypeId(Long.valueOf(issueFields.issueType().id()));
        if (!CaptureUtil.isTenantGDPRComplaint() && issueFields.assignee() != null
                && issueFields.assigneeAccountId() != null && !issueFields.assigneeAccountId().id().equalsIgnoreCase("-1")) {
            issueInputBuilder.setAssigneeName(issueFields.assignee().id());
        }
        if (CaptureUtil.isTenantGDPRComplaint() && issueFields.assigneeAccountId() != null &&
                !issueFields.assigneeAccountId().id().equalsIgnoreCase("-1")) {
        	CaptureUser user = userService.findUserByAccountId(issueFields.assigneeAccountId().id());
        	if(user != null) {
        		try {
					issueInputBuilder.setAssignee(new BasicUser(new URI(user.getSelf()), user.getName(), user.getDisplayName()));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
        	}            
        }
        Project project = projectService.getProjectObjByKey(issueFields.project().id());
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
            configCustomFields(host, issueInputBuilder, issueFields, project);
        }

        return issueInputBuilder.build();
    }

    private void configCustomFields(AtlassianHostUser host, IssueInputBuilder issueInputBuilder, IssueFields issueFields, Project issueProject){
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();
        String metadata =  metadataService.getMetaDataCacheOrFresh(hostUser,
                issueProject.getKey(),issueProject.getId());
        JsonNode metadataNode = null;
        if(StringUtils.isEmpty(metadata)){ return; }
        try {
             metadataNode = new ObjectMapper()
                    .readerFor(JsonNode.class)
                    .readValue(metadata);
        }catch (IOException ex){
            log.error("Error during converting metadata {}",ex.getMessage());
        }

        ArrayNode projects = (ArrayNode)metadataNode.get("projects");
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
                if(fieldValue instanceof String[] && fieldValue.length > 0) {
                    String fieldType = fields.get(fieldId).get("schema").get("type").asText();
                    String items = fields.get(fieldId).get("schema").get("items") != null ? fields.get(fieldId).get("schema").get("items").asText() : "";
                    String custom = fields.get(fieldId).get("schema").has("custom") ? fields.get(fieldId).get("schema").get("custom").asText() : "";
                    if (StringUtils.equals(fieldType, "string")) {
                        issueInputBuilder.setFieldValue(fieldId, fieldValue[0]);
                    } else if (StringUtils.equals(fieldType, "option") || StringUtils.equals(fieldType, "version")) {
                        if (fieldValue[0] != null && fieldValue[0].length() > 0) {
                            Map<String, Object> optionValue = new HashMap<>();
                            optionValue.put("id", fieldValue[0]);
                            issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(optionValue));
                        }
                    } else if (StringUtils.equals(fieldType, "any") && StringUtils.isNotBlank(fieldValue[0])) {
                        issueInputBuilder.setFieldValue(fieldId, fieldValue[0]);
                    } else if (StringUtils.equals(fieldType, "array") && (StringUtils.equals(items, "option") || StringUtils.equals(items, "version"))) {
                        List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                        List<String> values = Arrays.asList(fieldValue);
                        values.stream().forEach((value) -> {
                            Map<String, Object> complexValue = new TreeMap<>();
                            complexValue.put("id", value);
                            checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                        });
                        issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                    } else if (StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "group")) {
                        List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                        List<String> values = Arrays.asList(fieldValue);
                        values.stream().forEach((value) -> {
                            Map<String, Object> complexValue = new TreeMap<>();
                            complexValue.put("name", value);
                            checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                        });
                        issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                    } else if (StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "user")) {
                        List<ComplexIssueInputFieldValue> checkboxValues = new ArrayList<>();
                        List<String> values = Arrays.asList(fieldValue);
                        values.stream().forEach((value) -> {
                            String[] arrs = value.trim().split(",");
                            if (arrs != null && arrs.length > 0) {
                                for (String name : value.trim().split(",")) {
                                    if (!StringUtils.isEmpty(name)) {
                                        Map<String, Object> complexValue = new TreeMap<>();
                                        if (StringUtils.equals(items, "user") && acHostModel.getMigrated() != null && acHostModel.getMigrated().equals(AcHostModel.GDPRMigrationStatus.GDPR)) {
                                            complexValue.put("id", name.trim());
                                        } else {
                                            complexValue.put("name", name);
                                        }
                                        checkboxValues.add(new ComplexIssueInputFieldValue(complexValue));
                                    }
                                }
                            }
                        });
                        issueInputBuilder.setFieldValue(fieldId, checkboxValues);
                    } else if (StringUtils.equals(fieldType, "date") && StringUtils.isNotBlank(fieldValue[0])) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy");
                        Date date = sdf.parse(fieldValue[0]);
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStr = sdf.format(date);
                        issueInputBuilder.setFieldValue(fieldId, dateStr);
                    } else if (StringUtils.equals(fieldType, "datetime")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy hh:mm a");
                        //sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = sdf.parse(fieldValue[0]);
                        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        String dateStr = sdf.format(date);
                        issueInputBuilder.setFieldValue(fieldId, dateStr);
                    } else if (StringUtils.equals(fieldType, "array") && StringUtils.equals(items, "string")) {
                        List<String> values = Arrays.asList(fieldValue);
                        //Added check to see is this field is sprint then dont sent amy empty value
                        if (custom.equals("com.pyxis.greenhopper.jira:gh-sprint")) {
                            try {
                                //for sprint fieldType array and items is string
                                //but sprint value is long so just check if we
                                //can convert into long to get sprintId
                                if (StringUtils.isNotBlank(values.get(0))) {
                                    Long sprintId = Long.valueOf(values.get(0));
                                    issueInputBuilder.setFieldValue(fieldId, sprintId);
                                }
                            } catch (Exception e) {
                                // issueInputBuilder.setFieldValue(fieldId, values);
                            }
                        } else {
                            issueInputBuilder.setFieldValue(fieldId, values);
                        }

                    } else if (StringUtils.equals(fieldType, "number")) {
                        String numberStr = fieldValue[0];
                        if (StringUtils.isNotBlank(numberStr)) {
                            Double number = Double.valueOf(numberStr);
                            issueInputBuilder.setFieldValue(fieldId, number);
                        }
                    } else if (StringUtils.equals(fieldType, "user") || StringUtils.equals(fieldType, "group")) {
                        Map<String, Object> complexValue = new TreeMap<>();
                        if (acHostModel.getMigrated() != null && acHostModel.getMigrated().equals(AcHostModel.GDPRMigrationStatus.GDPR)) {
                            if (!"".equals(fieldValue[0].trim())) {
                                complexValue.put("id", fieldValue[0]);
                                issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                            }
                        } else {
                            if (!"".equals(fieldValue[0].trim())) {
                                complexValue.put("name", fieldValue[0]);
                                issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                            }
                        }
                    } else if (StringUtils.equals(fieldType, "option-with-child") && StringUtils.isNotBlank(fieldValue[0]) && StringUtils.isNotBlank(fieldValue[1])) {
                        Map<String, Object> complexValue = new TreeMap<>();
                        complexValue.put("id", fieldValue[0]);
                        Map<String, Object> childValue = new TreeMap<>();
                        childValue.put("id", fieldValue[1]);
                        complexValue.put("child", new ComplexIssueInputFieldValue(childValue));
                        issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                    } else if (StringUtils.equals(fieldType, "project")) {
                        Map<String, Object> complexValue = new TreeMap<>();
                        complexValue.put("id", fieldValue[0]);
                        issueInputBuilder.setFieldValue(fieldId, new ComplexIssueInputFieldValue(complexValue));
                    } else {
                        log.warn("This custom field type not supporting so skied field id : {} ,  field type:  {}", fieldId, fieldType);
                    }
                }
            } catch (Exception exception) {
                log.error("Error during config custom field for Jira issue create request customFieldId:{} issueType:{}", entry.getKey(), issueFields.issueType().id(), exception);
            }
        }
    }

    private String buildIssueCacheKey(String issueIdOrKey) {
        return ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + issueIdOrKey;
    }
    
    private String buildIssueCacheKeyWithProperties(String issueIdOrKey) {
        return ApplicationConstants.ISSUE_CACHE_KEY_PROPERTIES_PREFIX + issueIdOrKey;
    }

    public JiraRestClient createPostJiraRestClient(AtlassianHostUser hostUser) {
        return cJiraRestClientFactory.createJiraPostRestClient(hostUser);
    }
}
