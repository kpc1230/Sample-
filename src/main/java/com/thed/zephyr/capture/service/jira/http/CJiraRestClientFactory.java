package com.thed.zephyr.capture.service.jira.http;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.thed.zephyr.capture.util.Global.TokenHolder;

/**
 * Created by Masud on 8/21/17.
 */
public interface CJiraRestClientFactory {
     JiraRestClient createJiraGetRestClient(AtlassianHostUser host);
     JiraRestClient createJiraPostRestClient(AtlassianHostUser host);
     JiraRestClient createJiraPostRestClient(AtlassianHostUser host, TokenHolder holder);
}
