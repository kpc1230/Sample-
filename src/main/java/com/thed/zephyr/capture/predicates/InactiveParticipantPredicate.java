package com.thed.zephyr.capture.predicates;

import com.atlassian.excalibur.model.Participant;
import com.google.common.base.Predicate;

public class InactiveParticipantPredicate implements Predicate<Participant> {
    public boolean apply(Participant input) {
        return input.hasLeft();
    }
}
