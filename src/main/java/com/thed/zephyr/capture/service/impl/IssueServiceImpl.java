package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.thed.zephyr.capture.model.jira.Issue;
import com.thed.zephyr.capture.service.IssueService;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Override
    public Issue getIssueObject(Long issueId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl()+ JiraConstants.REST_API_ISSUE+"/"+issueId;

        try {
            Issue response = restTemplate.getForObject(uri, Issue.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting issue from jira.", exception);
            throw exception;
        }
    }
}
