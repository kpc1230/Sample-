package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Session.Status;

/**
 * Compares the session objects using status property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class StatusSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

    public StatusSessionComparator(boolean ascending) {
        this.ascending = ascending;
    }

	@Override
	public int compare(Session first, Session second) {
	    if (Objects.isNull(first) && Objects.isNull(second)) {
	        return 0;
	    }
        if (Objects.isNull(first)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(second)) {
            return ascending ? 1 : -1;
        }
        Status firstStatus = first.getStatus();
        Status secondStatus = second.getStatus();
        if (Objects.isNull(firstStatus) && Objects.isNull(secondStatus)) {
            return 0;
        }
        if (Objects.isNull(firstStatus)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondStatus)) {
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
