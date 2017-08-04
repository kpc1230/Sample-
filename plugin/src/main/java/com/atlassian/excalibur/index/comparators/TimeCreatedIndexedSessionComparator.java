package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

public class TimeCreatedIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public TimeCreatedIndexedSessionComparator(boolean ascending) {
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
        Long firstCreated = first.getTimeCreatedMillis();
        Long secondCreated = second.getTimeCreatedMillis();
        if (firstCreated == null && secondCreated == null) {
            return 0;
        }
        if (firstCreated == null) {
            return ascending ? -1 : 1;
        }
        if (secondCreated == null) {
            return ascending ? 1 : -1;
        }
        if (firstCreated == secondCreated) {
            return 0;
        }
        if (firstCreated < secondCreated) {
            return ascending ? -1 : 1;
        }
        return ascending ? 1 : -1;
    }
}
