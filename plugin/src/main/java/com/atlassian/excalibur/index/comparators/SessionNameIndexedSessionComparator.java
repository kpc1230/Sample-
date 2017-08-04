package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

public class SessionNameIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public SessionNameIndexedSessionComparator(boolean ascending) {
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
        String firstSessionName = first.getName();
        String secondSessionName = second.getName();
        if (firstSessionName == null && secondSessionName == null) {
            return 0;
        }
        if (firstSessionName == null) {
            return ascending ? -1 : 1;
        }
        if (secondSessionName == null) {
            return ascending ? 1 : -1;
        }
        if (firstSessionName.equals(secondSessionName)) {
            return 0;
        }
        if (firstSessionName.compareTo(secondSessionName) < 0) {
            return ascending ? -1 : 1;
        }
        return ascending ? 1 : -1;
    }
}
