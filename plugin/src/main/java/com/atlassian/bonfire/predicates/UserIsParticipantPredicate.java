package com.atlassian.bonfire.predicates;

import com.atlassian.excalibur.model.Participant;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;

public class UserIsParticipantPredicate implements Predicate<Participant> {
    private ApplicationUser user;

    public UserIsParticipantPredicate(ApplicationUser user) {
        this.user = user;
    }

    @Override
    public boolean apply(Participant input) {
        return !input.hasLeft() && input.getUser().equals(user);
    }

}
