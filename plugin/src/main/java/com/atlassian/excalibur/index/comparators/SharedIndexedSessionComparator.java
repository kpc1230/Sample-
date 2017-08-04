package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

public class SharedIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public SharedIndexedSessionComparator(boolean ascending) {
        this.ascending = ascending;
    }

    public int compare(IndexedSession first, IndexedSession second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return ascending ? -1 : 1;
        }
        if (second == null) {
            return ascending ? 1 : -1;
        }
        boolean firstSessionShared = first.isShared();
        boolean secondSessionShared = second.isShared();
        if (firstSessionShared == secondSessionShared) {
            return 0;
        }
        if (firstSessionShared) {
            return ascending ? 1 : -1;
        } else {
            return ascending ? -1 : 1;
        }
    }
}
