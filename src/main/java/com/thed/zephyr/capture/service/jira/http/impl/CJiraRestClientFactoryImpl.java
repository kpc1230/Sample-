package com.thed.zephyr.capture.service.jira.http.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.service.jira.http.JwtGetAuthenticationHandler;
import com.thed.zephyr.capture.service.jira.http.JwtPostAuthenticationHandler;
import com.thed.zephyr.capture.util.Global.TokenHolder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TokenHolder tokenHolder;

    @Override
    public JiraRestClient createJiraGetRestClient(AtlassianHostUser host) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient client = factory.create(URI.create(host.getHost().getBaseUrl()),
                new JwtGetAuthenticationHandler(host, ad));
        return client;
    }

    @Override
    public JiraRestClient createJiraPostRestClient(AtlassianHostUser host) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient client = factory.create(URI.create(host.getHost().getBaseUrl()),
                new JwtPostAuthenticationHandler(host, ad, tokenHolder));
        return client;
    }


}
