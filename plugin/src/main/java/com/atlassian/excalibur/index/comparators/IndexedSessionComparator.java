package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

/**
 * Default Comparator for Indexed Sessions
 */
public class IndexedSessionComparator implements Comparator<IndexedSession> {
    public int compare(IndexedSession indexedSession, IndexedSession indexedSession1) {
        // TODO Work out the ordering here
        return (indexedSession.getId() > indexedSession1.getId()) ? -1 : 1;
    }
}
