package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.events.GenericAnalyticsEvent;
import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test to parse event data
 *
 * @since v2.8.1
 */
public class AnalyticsResourceTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    I18nHelper i18nHelper;

    @Mock
    BonfireLicenseService bonfireLicenseService;

    @Mock
    JiraAuthenticationContext authContext;

    @Mock
    EventPublisher eventPublisher;

    @Mock
    ApplicationUser user;

    @InjectMocks
    private AnalyticsResource resource = new AnalyticsResource();

    @Before
    public void setUp() throws Exception {
        when(bonfireLicenseService.getLicenseStatusErrors()).thenReturn(new ErrorCollection());
        when(authContext.getI18nHelper()).thenReturn(i18nHelper);
        when(authContext.getUser()).thenReturn(user);
    }

    @Test
    public void testParseAnalyticsDataNoEventData() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', null]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
    }

    @Test
    public void testParseAnalyticsDataStringData() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', 'annotation.add']"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
    }

    @Test
    public void testParseAnalyticsDataSomeData() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'action' : 'added_attachment'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("added_attachment", genericAnalyticsEvent.getAction());
    }

    @Test
    public void testParseAnalyticsDataWithCount() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'source' : 'browser', count: '5'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("browser", genericAnalyticsEvent.getSource());
        assertEquals(Long.valueOf(5L), genericAnalyticsEvent.getCount());
    }

    @Test
    public void testParseAnalyticsDataWithCountBig() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'source' : 'browser', count: '2151515215215155'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("browser", genericAnalyticsEvent.getSource());
        assertEquals(Long.valueOf(2151515215215155L), genericAnalyticsEvent.getCount());
    }

    @Test
    public void testParseAnalyticsDataWithNullCount() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'source' : 'browser', count: 'null'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("browser", genericAnalyticsEvent.getSource());
        assertNull("No value have to be parsed", genericAnalyticsEvent.getCount());
    }

    @Test
    public void testParseAnalyticsDataWithCorruptedData() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'source' : 'browser', count: '0x124'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("browser", genericAnalyticsEvent.getSource());
        assertNull("No value have to be parsed", genericAnalyticsEvent.getCount());
    }

    @Test
    public void testParseAnalyticsDataBrowser() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'browser' : 'ie'}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertEquals("ie", genericAnalyticsEvent.getBrowser());
    }

    @Test
    public void testParseAnalyticsDataBrowserEmpty() throws Exception {
        final GenericAnalyticsEvent genericAnalyticsEvent = resource.parseAnalyticsData(new JSONArray("['jira.capture.sample', {'browser' : ''}]"));
        assertEquals("jira.capture.sample", genericAnalyticsEvent.calculateEventName());
        assertNull("Browser must be null", genericAnalyticsEvent.getBrowser());
    }

    @Test
    public void testParseAndFireEvent() throws Exception {
        resource.createSessionRequest("{events: [['jira.capture.sample', {'action' : 'added_attachment'}]]}");
        verify(eventPublisher).publish(anyObject());
    }
}
