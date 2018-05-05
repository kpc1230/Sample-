package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by Masud on 8/16/17.
 */
@Component
public class Global {

    @Autowired
    private Logger log;

    @Bean
    public boolean onGlobalStart() {
        log.info("Global Started");

        //load or initialize something from here
        //customMethod()

        return true;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AcHostModel createAcHostModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
        	AtlassianHostUser atlassianHostUser = (AtlassianHostUser) auth.getPrincipal();
        	AcHostModel acHostModel = (AcHostModel) atlassianHostUser.getHost();
            if (acHostModel != null) {
                return new AcHostModel(acHostModel);
            }
        }
        AcHostModel acHostModel1 = new AcHostModel();
        acHostModel1.setBaseUrl("http://unknownHost/jira");
        return acHostModel1;
    }

    @Autowired
    private CJiraRestClientFactory cJiraRestClientFactory;

    @Bean(destroyMethod="close")
    @Primary
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JiraRestClient createGetJiraRestClient(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
            if (host != null) {
                return cJiraRestClientFactory.createJiraGetRestClient(host);
            }
        }
        return null;
    }

    /**
     * To create POST JIRA Rest Client use as follows
     * // @Autowired
     * // @Qualifier("jiraRestClientPOST")
     * // private JiraRestClient jiraRestClient;
     *
     * @return
     */
    @Bean(name = "jiraRestClientPOST", destroyMethod="close")
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JiraRestClient createPostJiraRestClient(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
            if (host != null) {
                return cJiraRestClientFactory.createJiraPostRestClient(host);
            }
        }
        return null;
    }
}
