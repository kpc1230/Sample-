package com.thed.zephyr.capture.predicates;

import com.google.common.base.Predicate;
import com.thed.zephyr.capture.model.Participant;

/**
 * Predicate to check whether participant user and the current logged in user are same 
 * and left the session.
 * 
 * @author manjunath
 * @see com.google.common.base.Predicate 
 */
public class UserIsParticipantPredicate implements Predicate<Participant> {
	
    private String user;

    public UserIsParticipantPredicate(String user) {
        this.user = user;
    }

    @Override
    public boolean apply(Participant input) {
        return !input.hasLeft() && input.getUser().equals(user);
    }

}