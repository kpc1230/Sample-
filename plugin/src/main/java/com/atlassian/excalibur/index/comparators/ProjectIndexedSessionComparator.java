package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

/**
 * Comparator for the ProjectTabPanel
 *
 * @since v1.3
 */
public class ProjectIndexedSessionComparator implements Comparator<IndexedSession> {
    public int compare(IndexedSession indexedSession, IndexedSession indexedSession1) {
        // TODO Work out the ordering here
        return (indexedSession.getId() > indexedSession1.getId()) ? -1 : 1;
    }
}
