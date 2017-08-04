package com.atlassian.bonfire.conditions;


import com.atlassian.bonfire.customfield.BonfireContextCustomFieldsService;
import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.customfield.BonfireSessionCustomFieldService;
import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

public class BonfireIssueWebPanelCondition implements Condition {
    private static final String ISSUE_KEY = "issue";
    private static final String USER_KEY = "user";

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = BonfireContextCustomFieldsService.SERVICE)
    private BonfireContextCustomFieldsService bonfireContextCustomFieldsService;

    @Override
    public void init(Map<String, String> arg0) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> jiraFields) {
        Issue issue = (Issue) jiraFields.get(ISSUE_KEY);
        final Object user = jiraFields.get(USER_KEY);
        ApplicationUser appUser = user instanceof ApplicationUser ? (ApplicationUser) user : ApplicationUsers.from((User) user);
        CustomField raisedIn = bonfireSessionCustomFieldService.getRaisedInSessionCustomField();
        CustomField relatedTo = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        // If either of these have a value then we want to show the panel
        return (relatedTo.hasValue(issue) || hasRaisedInValue(appUser, raisedIn.getValueFromIssue(issue)))
                || bonfireContextCustomFieldsService.hasContextValues(issue);
    }

    private boolean hasRaisedInValue(ApplicationUser user, String raisedInValue) {
        if (!StringUtils.isBlank(raisedInValue)) {
            LightSession session = sessionController.getLightSession(raisedInValue);
            if (session != null) {
                if (bonfirePermissionService.canSeeSession(user, session)) {
                    return true;
                }
            }
        }
        return false;
    }
}
