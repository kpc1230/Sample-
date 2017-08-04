package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;

import java.util.Comparator;

public class ProjectNameIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public ProjectNameIndexedSessionComparator(boolean ascending) {
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
        String firstProject = first.getProjectName();
        String secondProject = second.getProjectName();
        if (firstProject == null && secondProject == null) {
            return 0;
        }
        if (firstProject == null) {
            return ascending ? -1 : 1;
        }
        if (secondProject == null) {
            return ascending ? 1 : -1;
        }
        if (firstProject.equals(secondProject)) {
            return 0;
        }
        if (firstProject.compareTo(secondProject) < 0) {
            return ascending ? -1 : 1;
        }
        return ascending ? 1 : -1;
    }
}
