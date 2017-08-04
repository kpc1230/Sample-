package com.atlassian.excalibur.web.util;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.JiraDurationUtils;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class TestExcaliburWebUtil {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    JiraDurationUtils jiraDurationUtils;

    @InjectMocks
    ExcaliburWebUtil excaliburWebUtil = new ExcaliburWebUtil();

    @Before
    public void setUp() throws Exception {
        when(applicationProperties.getDefaultBackedString(eq(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY))).thenReturn("24");
        when(applicationProperties.getDefaultBackedString(eq(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK))).thenReturn("7");
        when(applicationProperties.getDefaultBackedString(eq(APKeys.JIRA_TIMETRACKING_FORMAT))).thenReturn(JiraDurationUtils.FORMAT_PRETTY);

        // Using real jiraDurationUtils is not possible because of different constructors in 6.0 and 6.3
        when(jiraDurationUtils.getShortFormattedDuration(0L)).thenReturn("0m");
    }

    @Test
    public void testFormatShortTimeSpent() {
        assertEquals("0m", excaliburWebUtil.formatShortTimeSpent(new Duration(0L, 0L)));
        assertEquals("0m", excaliburWebUtil.formatShortTimeSpent(new Duration(0L, 1L)));
        assertEquals("0m", excaliburWebUtil.formatShortTimeSpent(new Duration(0L, 59000L)));
        assertEquals("1m", excaliburWebUtil.formatShortTimeSpent(new Duration(0L, 60000L)));
    }

}
