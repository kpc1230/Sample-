package com.thed.zephyr.capture.customfield.searcher;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.pocketknife.annotations.lucene.LuceneUsage;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Adds the values in the custom field to the document. We split the string up into individual id's and add them
 */
@LuceneUsage(type = LuceneUsage.LuceneUsageType.Indexer)
public class SessionCustomFieldIndexer extends AbstractCustomFieldIndexer {
    protected SessionCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField) {
        super(fieldVisibilityManager, customField);
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
        String sessions = (String) customField.getValue(issue);
        if (sessions == null) {
            return;
        }

        // Split the string and add it to the document. SingleSessionCFT will work here too...
        String[] split = sessions.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
        for (String sessionId : split) {
            if (StringUtils.isNotBlank(sessionId)) {
                doc.add(new Field(getDocumentFieldId(), sessionId, Field.Store.YES, indexType));
            }
        }
    }
}
