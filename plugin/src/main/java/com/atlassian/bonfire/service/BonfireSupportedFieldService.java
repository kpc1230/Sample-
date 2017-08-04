package com.atlassian.bonfire.service;

import com.atlassian.bonfire.service.parser.FieldDetailsParser;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;

import static com.atlassian.bonfire.web.util.JiraFieldConstants.*;

@Service(BonfireSupportedFieldService.SERVICE)
public class BonfireSupportedFieldService {
    public static final String SERVICE = "bonfire-BonfireSupportedFieldService";

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private final HashSet<String> supportedCustomFields;

    @Resource(name = FieldDetailsParser.SERVICE)
    private FieldDetailsParser fieldDetailsParser;

    public BonfireSupportedFieldService() {
        // IMPORTANT this needs to be updated with any new additions into JiraFieldConstants
        supportedCustomFields = Sets.newHashSet(ASSIGNEE, RESOLUTION, FIX_VERSION, AFFECTS_VERSION, COMPONENTS, SECURITY, PRIORITY, USER_PICKER_KEY,
                CASCADING_SELECT_KEY, DATE_PICKER_KEY, DATE_TIME_KEY, FREE_TEXT_AREA_KEY, GROUP_PICKER_KEY, LABEL_KEY, MULTI_GROUP_PICKER_KEY,
                MULTI_USER_PICKER_KEY, MULTI_CHECKBOX_KEY, MULTI_SELECT_KEY, MULTI_VERSION_KEY, VERSION_KEY, NUMBER_KEY, PROJECT_PICKER_KEY,
                RADIO_BUTTON_KEY, SELECT_KEY, TEXT_FIELD_KEY, URL_FIELD_KEY, ASSIGNEE, SUMMARY, ISSUE_TYPE, REPORTER, ENVIRONMENT, LABELS,
                DESCRIPTION, TIME_TRACKING, DUE_DATE, ATTACHMENT, WORKLOG, ISSUELINKS, BONFIRE_SESSION, BONFIRE_MULTI_SESSION, BONFIRE_TEXT, EPIC_LINK);
    }

    /**
     * Checks whether Capture for JIRA supports rendering of the field key
     *
     * @param key field key
     * @return true if field is supported
     */
    public boolean isFieldSupported(String key) {
        return supportedCustomFields.contains(key);
    }

    /**
     * Checks whether field can be parsed from the rendered field HTML
     *
     * @param project                     project to render field for
     * @param issueType                   issue to render field for
     * @param fieldScreenRenderLayoutItem renderer layout item
     */
    public boolean isAdvancedCustomFieldSupported(Project project, IssueType issueType, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        return null != fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
    }
}
