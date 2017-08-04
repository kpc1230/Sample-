package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

public class AssigneeNameIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public AssigneeNameIndexedSessionComparator(boolean ascending) {
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
        String firstAssignee = first.getAssigneeDisplayName();
        String secondAssignee = second.getAssigneeDisplayName();
        if (firstAssignee == null && secondAssignee == null) {
            return 0;
        }
        if (firstAssignee == null) {
            return ascending ? -1 : 1;
        }
        if (secondAssignee == null) {
            return ascending ? 1 : -1;
        }
        if (firstAssignee.equals(secondAssignee)) {
            return 0;
        }
        if (firstAssignee.compareTo(secondAssignee) < 0) {
            return ascending ? -1 : 1;
        }
        return ascending ? 1 : -1;
    }
}
