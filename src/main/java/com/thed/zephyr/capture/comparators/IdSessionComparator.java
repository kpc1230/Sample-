package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;

/**
 * Compares the session objects using session id property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class IdSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

    public IdSessionComparator(boolean ascending) {
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
        String firstSessionId = first.getId();
        String secondSessionId = second.getId();
        
        if (Objects.isNull(firstSessionId) && Objects.isNull(secondSessionId)) {
            return 0;
        }
        
        if (Objects.isNull(firstSessionId)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondSessionId)) {
            return ascending ? 1 : -1;
        }
        if(ascending) {
        	return firstSessionId.compareTo(secondSessionId);
        } else {
        	return secondSessionId.compareTo(firstSessionId); 
        }
	}
}
