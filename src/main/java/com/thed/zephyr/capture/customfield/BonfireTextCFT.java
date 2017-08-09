package com.thed.zephyr.capture.customfield;

import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;

/**
 * This custom field type is IDENTICAL to TextCFType except we define the vms used to render them
 */
public class BonfireTextCFT extends TextCFType {
    public BonfireTextCFT(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        // Let's make it unlimited because we can
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }
}
