package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.internal.json.IssuelinksTypeJsonParserV5;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.service.jira.IssueLinkTypeService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.JiraConstants;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class IssueLinkTypeServiceImpl implements IssueLinkTypeService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;

    @Autowired
    private CJiraRestClientFactory cJiraRestClientFactory;
    @Autowired
    private CaptureI18NMessageSource i18n;


    @Override
    public List<IssuelinksType> getIssuelinksType(AtlassianHostUser hostUser) {
        return Lists.newArrayList(createGetJiraRestClient(hostUser).getMetadataClient().getIssueLinkTypes().claim());
    }

    @Override
    public IssuelinksType createIssuelinkType(IssuelinksType issuelinksType, AtlassianHostUser hostUser) {
        String url = hostUser.getHost().getBaseUrl() + JiraConstants.REST_API_ISSUE_LINK_TYPE;
        JSONObject reqJson = new JSONObject();
        ResponseEntity<String> resp = null;
        IssuelinksType issueLinkType = null;
        try {
            reqJson.put("name", issuelinksType.getName());
            reqJson.put("inward", issuelinksType.getInward());
            reqJson.put("outward", issuelinksType.getOutward());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestUpdate = new HttpEntity<>(reqJson.toString(), httpHeaders);
            resp = atlassianHostRestClients.authenticatedAs(hostUser).exchange(url, HttpMethod.POST, requestUpdate, String.class);
            String jsonNode = resp.getBody();
            JSONObject jsonObject = new JSONObject(jsonNode);
            IssuelinksTypeJsonParserV5 issuelinksTypeJsonParserV5 = new IssuelinksTypeJsonParserV5();
            issueLinkType = issuelinksTypeJsonParserV5.parse(jsonObject);
        } catch (Exception exp) {
            log.error("Error in linkIssue : " + exp.getMessage(), exp);
        }

        return issueLinkType;
    }

    public JiraRestClient createGetJiraRestClient(AtlassianHostUser hostUser) {
        return cJiraRestClientFactory.createJiraGetRestClient(hostUser);
    }

    @Override
    public void createIssuelinkTypeCaptureTestingIfNotExist(AtlassianHostUser hostUser) {
        try {
            List<IssuelinksType> linkTypes = getIssuelinksType(hostUser);
            IssuelinksType linkType = null;
            try {
                linkType = linkTypes.stream().filter(o -> o.getName().equals(ApplicationConstants.CAPTURE_TESTING_ISSUE_LINKTYPE)).findFirst().get();
            } catch (NoSuchElementException exp) {
                log.error("The Issue Types not exist adding one", exp.getMessage());
                String outgoingName = i18n.getMessage("bonfire.issue.link.outgoing");
                String incomingName = i18n.getMessage("bonfire.issue.link.incoming");
                IssuelinksType createReq = new IssuelinksType(null, null, ApplicationConstants.CAPTURE_TESTING_ISSUE_LINKTYPE, incomingName, outgoingName);
                IssuelinksType resp1 = createIssuelinkType(createReq, hostUser);
            }
        } catch (Exception exception) {
            log.error("Error creating custom linktype. " + ApplicationConstants.CAPTURE_TESTING_ISSUE_LINKTYPE, exception);
        }
    }
}
