package com.thed.zephyr.capture.service.jira.http.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.thed.zephyr.capture.model.be.BEAuthToken;
import com.thed.zephyr.capture.model.be.BEContextAuthentication;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.service.jira.http.JwtGetAuthenticationHandler;
import com.thed.zephyr.capture.service.jira.http.JwtPostAuthenticationHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Created by Masud on 8/21/17.
 */
@Component
public class CJiraRestClientFactoryImpl implements CJiraRestClientFactory {

    @Autowired
    private Logger log;
    @Autowired
    private AddonDescriptorLoader ad;
    @Autowired
    private UserService userService;

    @Override
    public JiraRestClient createJiraGetRestClient(AtlassianHostUser host) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        return factory.create(URI.create(host.getHost().getBaseUrl()), new JwtGetAuthenticationHandler(host, ad));
    }

    @Override
    public JiraRestClient createJiraPostRestClient(AtlassianHostUser host) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient client;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof BEContextAuthentication){
            BEAuthToken beAuthToken = ((BEContextAuthentication) auth).getBeAuthToken();
            if(beAuthToken.getApiToken() != null){
                CaptureUser userByKey = userService.findUserByKey(beAuthToken.getUserKey());
                String userName = StringUtils.isNotBlank(userByKey.getEmailAddress()) ? userByKey.getEmailAddress() : beAuthToken.getBeLoggedInParam();
                client = factory.createWithBasicHttpAuthentication(URI.create(host.getHost().getBaseUrl()), userName, beAuthToken.getApiToken());
            } else {
                client = factory.create(URI.create(host.getHost().getBaseUrl()), new JwtPostAuthenticationHandler(host, ad, beAuthToken.getJiraToken()));
            }
        } else {
            client = factory.create(URI.create(host.getHost().getBaseUrl()), new JwtPostAuthenticationHandler(host, ad, null));
        }

        return client;
    }
}
