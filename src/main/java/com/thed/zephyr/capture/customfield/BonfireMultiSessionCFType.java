package com.thed.zephyr.capture.customfield;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.atlassian.excalibur.model.Session;
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
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BonfireMultiSessionCFType extends StringCFType {
    private final SessionController sessionController;
    private final ExcaliburWebUtil excaliburWebUtil;

    public BonfireMultiSessionCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
                                     SessionController sessionController, ExcaliburWebUtil excaliburWebUtil) {
        super(customFieldValuePersister, genericConfigManager);
        this.sessionController = sessionController;
        this.excaliburWebUtil = excaliburWebUtil;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        String value = field.getValueFromIssue(issue);
        if (!StringUtils.isEmpty(value)) {
            String[] split = value.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            List<SessionCFTTransportObject> sessions = Lists.newArrayList();
            for (String s : split) {
                if (!StringUtils.isEmpty(s)) {
                    SessionResult result = sessionController.getSessionWithoutNotes(s);
                    if (result.isValid()) {
                        Session session = result.getSession();
                        ApplicationUser assignee = session.getAssignee();
                        sessions.add(new SessionCFTTransportObject(session.getId().toString(), BonfireConstants.SESSION_PAGE + s, session.getName(),
                                getUserAvatarUrl(assignee),
                                assignee.getName(), session.getStatus().toString()));
                    }
                }
            }
            map.put("sessions", sessions);
        }
        return map;
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        // can never be too careful.. don't know why an issue would have over 100 sessions but who knows.
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    @Override
    public String getStringFromSingularObject(Object singularObject) {
        return (String) singularObject;
    }

    @Override
    public Object getSingularObjectFromString(String string) throws FieldValidationException {
        return string;
    }

    private String getUserAvatarUrl(ApplicationUser user) {
        return excaliburWebUtil.getSmallAvatarUrl(user);
    }
}
