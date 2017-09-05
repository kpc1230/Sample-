package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;

/**
 * Compares the session objects using assignee property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class AssigneeSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

    public AssigneeSessionComparator(boolean ascending) {
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
        String firstAssignee = first.getAssignee();
        String secondAssignee = second.getAssignee();
        
        if (Objects.isNull(firstAssignee) && Objects.isNull(secondAssignee)) {
            return 0;
        }
        if (Objects.isNull(firstAssignee)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondAssignee)) {
            return ascending ? 1 : -1;
        }
        if(ascending) {
        	return firstAssignee.compareTo(secondAssignee);
        } else {
        	return secondAssignee.compareTo(firstAssignee); 
        }
	}

}
