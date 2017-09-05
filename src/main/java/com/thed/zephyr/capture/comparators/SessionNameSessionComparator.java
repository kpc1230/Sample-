package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;

/**
 * Compares the session objects using session name property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class SessionNameSessionComparator implements Comparator<Session>  {
	
	private boolean ascending;

    public SessionNameSessionComparator(boolean ascending) {
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
        String firstSessionName = first.getName();
        String secondSessionName = second.getName();
        if (Objects.isNull(firstSessionName) && Objects.isNull(secondSessionName)) {
            return 0;
        }
        if (Objects.isNull(firstSessionName)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondSessionName)) {
            return ascending ? 1 : -1;
        }
        if(ascending) {
        	return firstSessionName.compareTo(secondSessionName);
        } else {
        	return secondSessionName.compareTo(firstSessionName); 
        }
    }
}
