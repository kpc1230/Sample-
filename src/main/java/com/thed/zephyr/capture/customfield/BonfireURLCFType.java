package com.thed.zephyr.capture.customfield;

import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;

/**
 * If we no longer need to extend URLCFType then instead extend GenericTextCFType This class is pretty much the same as BonfireTextCFT except this
 * needs to remain here for legacy reasons. This class will need to remain here with this name for as long as we use this field
 */
public class BonfireURLCFType extends TextCFType {
    public BonfireURLCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }
}
