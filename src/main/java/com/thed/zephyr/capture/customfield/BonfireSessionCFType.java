package com.thed.zephyr.capture.customfield;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.SessionResult;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.StringCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class BonfireSessionCFType extends StringCFType {
    private final SessionController sessionController;
    private final ExcaliburWebUtil excaliburWebUtil;

    public BonfireSessionCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
                                SessionController sessionController, ExcaliburWebUtil excaliburWebUtil) {
        super(customFieldValuePersister, genericConfigManager);
        this.sessionController = sessionController;
        this.excaliburWebUtil = excaliburWebUtil;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        String value = field.getValueFromIssue(issue);
        SessionResult result = sessionController.getSessionWithoutNotes(value);
        if (result.isValid()) {
            map.put("sessionURL", BonfireConstants.SESSION_PAGE + value);
            map.put("sessionName", result.getSession().getName());
            map.put("iconUrl", getUserAvatarUrl(result.getSession().getAssignee()));
            map.put("hasSession", true);
        } else {
            map.put("hasSession", false);
        }
        return map;
    }

    /**
     * Overriden methods mostly copied from parent class.
     */
    @Override
    public String getStringFromSingularObject(final Object value) {
        assertObjectImplementsType(String.class, value);
        // convert null to empty string
        return StringUtils.defaultString((String) value);
    }

    @Override
    public Object getSingularObjectFromString(String string) throws FieldValidationException {
        return string;
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        // We are only storing ids so this should be more than enough
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    private String getUserAvatarUrl(ApplicationUser user) {
        return excaliburWebUtil.getSmallAvatarUrl(user);
    }
}
