package com.atlassian.excalibur.index.comparators;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session.Status;

import java.util.Comparator;

public class StatusIndexedSessionComparator implements Comparator<IndexedSession> {
    private boolean ascending;

    public StatusIndexedSessionComparator(boolean ascending) {
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
        Status firstStatus = first.getStatus();
        Status secondStatus = second.getStatus();
        if (firstStatus == null && secondStatus == null) {
            return 0;
        }
        if (firstStatus == null) {
            return ascending ? -1 : 1;
        }
        if (secondStatus == null) {
            return ascending ? 1 : -1;
        }
        int firstWeight = getStatusOrder(firstStatus);
        int secondWeight = getStatusOrder(secondStatus);
        if (firstWeight == secondWeight) {
            return 0;
        }
        if (firstWeight < secondWeight) {
            return ascending ? -1 : 1;
        }
        return ascending ? 1 : -1;
    }

    private int getStatusOrder(Status status) {
        switch (status) {
            case CREATED:
                return 1;
            case STARTED:
                return 2;
            case PAUSED:
                return 3;
            case COMPLETED:
                return 4;
        }
        return 0;
    }
}
