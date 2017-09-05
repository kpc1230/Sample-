package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;

/**
 * Compares the session objects using created time property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class TimeCreatedSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

    public TimeCreatedSessionComparator(boolean ascending) {
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
        Long firstCreated = first.getTimeCreated().getMillis();
        Long secondCreated = second.getTimeCreated().getMillis();
        
        if (Objects.isNull(firstCreated) && Objects.isNull(secondCreated)) {
            return 0;
        }
        
        if (Objects.isNull(firstCreated)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondCreated)) {
            return ascending ? 1 : -1;
        }
        
        if(ascending) {
        	return firstCreated.compareTo(secondCreated);
        } else {
        	return secondCreated.compareTo(firstCreated); 
        }
    }
}
