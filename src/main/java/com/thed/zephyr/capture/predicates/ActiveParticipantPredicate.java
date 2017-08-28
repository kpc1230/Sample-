package com.thed.zephyr.capture.predicates;

import com.google.common.base.Predicate;
import com.thed.zephyr.capture.model.Participant;

/**
 * Predicate to check whether participant user has left the session or not.
 * 
 * @author manjunath
 * @see com.google.common.base.Predicate
 */
public class ActiveParticipantPredicate implements Predicate<Participant> {
	
	@Override
    public boolean apply(Participant input) {
        return !input.hasLeft();
    }
}
