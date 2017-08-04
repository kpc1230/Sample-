package com.atlassian.bonfire.model;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestTemplate {
    @Test
    public void testEquality() {
        DateTime someTime = new DateTime();
        Template one = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        Template two = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);

        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
    }

    @Test
    public void testInequality() {
        DateTime someTime = new DateTime(100);
        Template correct = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        Template incorrect1 = new Template(1L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        Template incorrect2 = new Template(1000L, 1L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        Template incorrect3 = new Template(1000L, 1000L, "admin", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        Template incorrect4 = new Template(1000L, 1000L, "", "{}", Collections.<Variable>emptyList(), someTime, someTime, someTime, false);
        List<Variable> vars = new ArrayList<Variable>();
        vars.add(Variable.create(1L, "var", "js", "admin"));
        Template incorrect5 = new Template(1000L, 1000L, "", "", vars, someTime, someTime, someTime, false);
        Template incorrect6 = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), new DateTime(0), someTime, someTime, false);
        Template incorrect7 = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, new DateTime(101), someTime, false);
        Template incorrect8 = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, new DateTime(99), false);
        Template incorrect9 = new Template(1000L, 1000L, "", "", Collections.<Variable>emptyList(), someTime, someTime, someTime, true);

        assertFalse(correct.equals(incorrect1));
        assertFalse(correct.equals(incorrect2));
        assertFalse(correct.equals(incorrect3));
        assertFalse(correct.equals(incorrect4));
        assertFalse(correct.equals(incorrect5));
        assertFalse(correct.equals(incorrect6));
        assertFalse(correct.equals(incorrect7));
        assertFalse(correct.equals(incorrect8));
        assertFalse(correct.equals(incorrect9));
    }
}
