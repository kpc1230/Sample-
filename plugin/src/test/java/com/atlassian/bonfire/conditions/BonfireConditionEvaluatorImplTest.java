package com.atlassian.bonfire.conditions;

import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class BonfireConditionEvaluatorImplTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    BonfireLicenseService bonfireLicenseService;

    @InjectMocks
    BonfireConditionEvaluator conditionEvaluator = new BonfireConditionEvaluatorImpl();

    @Test
    public void testShouldDisplayLicense() throws Exception {
        when(bonfireLicenseService.isBonfireActivated()).thenReturn(false);
        assertThat(conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE), is(false));

        when(bonfireLicenseService.isBonfireActivated()).thenReturn(true);
        assertThat(conditionEvaluator.shouldDisplay(AccessCheckMode.LICENSE), is(true));
    }
}
