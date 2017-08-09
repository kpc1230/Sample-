package com.thed.zephyr.capture.customfield.searcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import webwork.action.Action;

import java.util.Map;

public class EmptySearchRenderer extends CustomFieldRenderer {
    public EmptySearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field,
                               CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager) {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
    }

    /**
     * @deprecated Use {@link EmptySearchRenderer#getEditHtml(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.search.SearchContext, com.atlassian.jira.issue.transport.FieldValuesHolder, java.util.Map, webwork.action.Action)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public String getEditHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        // deprecated: drop the moethod since JIRA 6.X support is dropped
        return getEditHtml(ApplicationUsers.from(searcher), searchContext, fieldValuesHolder, displayParameters, action);
    }

    /**
     * @deprecated Use {@link com.atlassian.bonfire.customfield.searcher.EmptySearchRenderer#getViewHtml(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.search.SearchContext, com.atlassian.jira.issue.transport.FieldValuesHolder, java.util.Map, webwork.action.Action)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public String getViewHtml(User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        // deprecated: drop the moethod since JIRA 6.X support is dropped
        return getViewHtml(ApplicationUsers.from(searcher), searchContext, fieldValuesHolder, displayParameters, action);
    }

    /**
     * @deprecated Use {@link EmptySearchRenderer#isRelevantForQuery(com.atlassian.jira.user.ApplicationUser, com.atlassian.query.Query)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public boolean isRelevantForQuery(User searcher, Query query) {
        // deprecated: drop the moethod since JIRA 6.X support is dropped
        return isRelevantForQuery(ApplicationUsers.from(searcher), query);
    }

    /**
     * @deprecated Use {@link EmptySearchRenderer#isShown(com.atlassian.jira.user.ApplicationUser, com.atlassian.jira.issue.search.SearchContext)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public boolean isShown(User searcher, SearchContext searchContext) {
        // deprecated: drop the moethod since JIRA 6.X support is dropped
        return isShown(ApplicationUsers.from(searcher), searchContext);
    }

    public String getEditHtml(ApplicationUser searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        // JIRA 7.0 compatibility
        return "";
    }

    public String getViewHtml(ApplicationUser searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map<?, ?> displayParameters,
                              Action action) {
        // JIRA 7.0 compatibility
        return "";
    }

    public boolean isRelevantForQuery(ApplicationUser searcher, Query query) {
        // JIRA 7.0 compatibility
        return false;
    }

    public boolean isShown(ApplicationUser searcher, SearchContext searchContext) {
        // JIRA 7.0 compatibility
        return false;
    }
}
