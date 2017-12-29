package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.util.concurrent.Promise;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.serverInfo.AddonServerInfo;
import com.thed.zephyr.capture.service.ServerInfoService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Masud on 9/15/17.
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    @Autowired
    private Logger log;

    @Value("${info.app.artifactId}")
    private String appName;

    @Value("${info.app.version}")
    private String appVersion;

    @Autowired
    private CJiraRestClientFactory jiraRestClientFactory;

    @Autowired
    private SessionESRepository sessionESRepository;

    @Override
    public AddonServerInfo getAddonServerInfo() {
        AddonServerInfo addonServerInfo =
                new AddonServerInfo(appName, appVersion);
        log.debug("SERVER INFO: {}, {}", appName, appVersion);
        return addonServerInfo;
    }

    @Override
    public Optional<ServerInfo> getJiraServerInfo(AcHostModel acHostModel) {
        JiraRestClient jiraRestClient = null;
        try{
            jiraRestClient = getJiraRestClient(acHostModel);
            Promise<ServerInfo> serverInfoPromise = jiraRestClient.getMetadataClient().getServerInfo();
            ServerInfo serverInfo = serverInfoPromise.claim();
            return Optional.ofNullable(serverInfo);
        } finally {
            if(jiraRestClient != null){
                try {
                    jiraRestClient.close();
                } catch (IOException exception) {
                    log.error("Error during getJiraServerInfo from Jira, not able to close jiraRestClient", exception);
                }
            }
        }

    }

    private JiraRestClient getJiraRestClient(AcHostModel acHostModel) {
        AtlassianHostUser atlassianHostUser = new AtlassianHostUser(acHostModel, Optional.ofNullable(null));
        JiraRestClient jiraRestClient = jiraRestClientFactory.createJiraGetRestClient(atlassianHostUser);
        return jiraRestClient;
    }

    @Override
    public Optional<Integer> getProjectsCount(AcHostModel acHostModel) {
        JiraRestClient jiraRestClient = null;
        try{
            jiraRestClient = getJiraRestClient(acHostModel);
            Promise<Iterable<BasicProject>> projectsPromise = jiraRestClient.getProjectClient().getAllProjects();
            Iterable<BasicProject> projects = projectsPromise.claim();
            if (null != projects && projects instanceof Collection<?>){
                return Optional.of(((Collection<?>) projects).size());
            }

            return Optional.of(0);
        } finally {
            if(jiraRestClient != null){
                try {
                    jiraRestClient.close();
                } catch (IOException exception) {
                    log.error("Error during getProjectsCount from Jira, not able to close jiraRestClient", exception);
                }
            }
        }
    }

    @Override
    public Optional<Integer> getIssuesCount(AcHostModel acHostModel) {
        JiraRestClient jiraRestClient = null;
        try{
            jiraRestClient = getJiraRestClient(acHostModel);
            SearchResult searchResult = jiraRestClient.getSearchClient().searchJql("").claim();
            if (null != searchResult)
                return Optional.of(searchResult.getTotal());

            return Optional.of(0);
        } finally {
            if(jiraRestClient !=null){
                try {
                    jiraRestClient.close();
                } catch (IOException exception) {
                    log.error("Error during getIssuesCount from Jira, not able to close jiraRestClient", exception);
                }
            }
        }

    }

    @Override
    public Optional<Integer> getAttachmentsCount(AcHostModel acHostModel) {
        JiraRestClient jiraRestClient = null;
        try{
            jiraRestClient = getJiraRestClient(acHostModel);
            List<Attachment> attachments = new ArrayList<>();
            SearchResult searchResult = jiraRestClient.getSearchClient().searchJql("").claim();
            if(searchResult.getIssues() != null) {
                searchResult.getIssues().forEach(issue -> {
                    log.debug("ISSUES(key,summary): {}, {}", issue.getKey(), issue.getSummary());
                    if(issue.getAttachments() != null) {
                        issue.getAttachments().forEach(attachment -> {
                            log.debug("ATTACHMENTS: {}", attachment.getFilename());
                        });
                        List<Attachment> attachmentList = (List<Attachment>) issue.getAttachments();
                        if (attachmentList != null && attachmentList.size() > 0) {
                            attachments.addAll(attachmentList);
                        }
                    }
                });
            }
            return Optional.of(attachments.size());
        } finally {
            if(jiraRestClient != null){
                try {
                    jiraRestClient.close();
                } catch (IOException exception) {
                    log.error("Error during getAttachmentsCount from Jira, not able to close jiraRestClient", exception);
                }
            }
        }
    }

    @Override
    public Optional<Integer> getSessionsCount(AcHostModel acHostModel) {
        return Optional.of((int) sessionESRepository.countByCtId(acHostModel.getCtId()));
    }


}
