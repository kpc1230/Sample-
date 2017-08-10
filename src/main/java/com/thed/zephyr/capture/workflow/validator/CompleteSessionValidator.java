package com.thed.zephyr.capture.workflow.validator;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

public class CompleteSessionValidator implements Validator {
    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = SessionController.SERVICE)
    private SessionController controller;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Override
    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException, WorkflowException {
        Issue issue = (Issue) transientVars.get("issue");
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        String value = relatedToField.getValueFromIssue(issue);
        if (StringUtils.isNotBlank(value)) {
            String[] split = value.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            int incompleteSessionCount = 0;
            for (String s : split) {
                LightSession ls = controller.getLightSession(s);
                if (!Status.COMPLETED.equals(ls.getStatus())) {
                    incompleteSessionCount++;
                }
            }
            if (incompleteSessionCount > 0) {
                if (incompleteSessionCount == 1) {
                    throw new WorkflowException(i18n.getText("bonfire.validator.complete.error.single"));
                } else {
                    throw new WorkflowException(i18n.getText("bonfire.validator.complete.error", incompleteSessionCount));
                }
            }
        }
    }
}
