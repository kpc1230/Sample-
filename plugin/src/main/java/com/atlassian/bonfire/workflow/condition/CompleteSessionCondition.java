package com.atlassian.bonfire.workflow.condition;

import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.model.LightSession;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

public class CompleteSessionCondition extends AbstractJiraCondition {
    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = SessionController.SERVICE)
    private SessionController controller;

    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        Issue issue = getIssue(transientVars);
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        String value = relatedToField.getValueFromIssue(issue);
        if (StringUtils.isNotBlank(value)) {
            String[] split = value.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            for (String s : split) {
                LightSession ls = controller.getLightSession(s);
                if (!Status.COMPLETED.equals(ls.getStatus())) {
                    return false;
                }
            }
        }
        return true;
    }
}
