package com.atlassian.bonfire.conditions;

import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.google.common.collect.Maps;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class BonfireActivatedConditionTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private BonfireConditionEvaluator conditionEvaluator;

    @InjectMocks
    private BonfireActivatedCondition condition = new BonfireActivatedCondition();

    @Test
    public void testShouldDisplay() throws Exception {
        when(conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE)).thenReturn(false);
        assertThat(condition.shouldDisplay(getParams()), is(false));

        when(conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE)).thenReturn(true);
        assertThat(condition.shouldDisplay(getParams()), is(true));
    }

    private Map<String, Object> getParams() {
        return Maps.newHashMap();
    }
}
