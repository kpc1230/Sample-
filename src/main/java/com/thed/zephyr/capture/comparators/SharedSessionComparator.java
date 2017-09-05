package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;

/**
 * Compares the session objects using shared property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class SharedSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

    public SharedSessionComparator(boolean ascending) {
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
