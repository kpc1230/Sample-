package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

/**
 * Comparator for the User session list
 *
 * @since v1.3
 */
public class UserIndexedSessionComparator implements Comparator<IndexedSession> {
    private static final Long NO_ACTIVE_ID = -1L;

    private Long activeId = NO_ACTIVE_ID;

    public UserIndexedSessionComparator() {
    }

    public UserIndexedSessionComparator(Long activeId) {
        this.activeId = activeId == null ? NO_ACTIVE_ID : activeId;
    }

    public int compare(IndexedSession indexedSession, IndexedSession indexedSession1) {
        // If we have explicitly set an activeId then we want that to be first
        if (!NO_ACTIVE_ID.equals(activeId)) {
            if (activeId.equals(indexedSession.getId())) {
                return -1;
            }
            if (activeId.equals(indexedSession1.getId())) {
                return 1;
            }
        }
        return (indexedSession.getId() > indexedSession1.getId()) ? -1 : 1;
    }
}
