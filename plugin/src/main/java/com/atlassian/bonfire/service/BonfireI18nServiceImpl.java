package com.atlassian.bonfire.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service(BonfireI18nService.SERVICE)
public class BonfireI18nServiceImpl implements BonfireI18nService {
    @JIRAResource
    JiraAuthenticationContext jiraAuthenticationContext;


    public Locale getLocale() {
        return jiraAuthenticationContext.getLocale();
    }

    public String getText(String key, Object... params) {
        return jiraAuthenticationContext.getI18nHelper().getText(key, params);
    }

    public void addError(ErrorCollection errorCollection, final String fieldName, String errorKey, Object... params) {
        errorCollection.addError(getText(errorKey, params), fieldName, errorKey);
    }
}
