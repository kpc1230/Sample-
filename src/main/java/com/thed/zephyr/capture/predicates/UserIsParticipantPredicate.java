package com.thed.zephyr.capture.predicates;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicate;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * Predicate to check whether participant user and the current logged in user are same 
 * and left the session.
 * 
 * @author manjunath
 * @see com.google.common.base.Predicate 
 */
public class UserIsParticipantPredicate implements Predicate<Participant> {
	
    private String user;
    private String userAccountId;

    public UserIsParticipantPredicate(String user, String userAccountId) {
        this.user = user;
        this.userAccountId = userAccountId;
    }

    @Override
    public boolean apply(Participant input) {
    	boolean isTenantGDPRComplaint = CaptureUtil.isTenantGDPRComplaint();
    	if(isTenantGDPRComplaint) {
    		return !input.hasLeft() && userAccountId.equals(input.getUserAccountId());
    	}
        return !input.hasLeft() && user.equals(input.getUser());
    }

}