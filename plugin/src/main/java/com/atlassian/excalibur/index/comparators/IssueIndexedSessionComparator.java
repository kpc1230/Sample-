package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

/**
 * Comparator for the IssueTabPanel
 *
 * @since v1.3
 */
public class IssueIndexedSessionComparator implements Comparator<IndexedSession> {
    public int compare(IndexedSession indexedSession, IndexedSession indexedSession1) {
        return (indexedSession.getId() > indexedSession1.getId()) ? -1 : 1;
    }
}
