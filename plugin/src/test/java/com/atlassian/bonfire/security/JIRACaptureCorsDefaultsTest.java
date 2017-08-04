package com.atlassian.bonfire.security;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JIRACaptureCorsDefaultsTest {

    private JIRACaptureCorsDefaults corsDefaults;

    @Before
    public void setUp() throws Exception {
        corsDefaults = new JIRACaptureCorsDefaults();
    }

    @Test
    public void verifyAllowsOriginChrome() throws Exception {
        assertTrue("Chrome Extension Origin must be allowed", corsDefaults.allowsOrigin("chrome-extension://oapgfmnbjlbhblfhbecgnljpcdpcdpki"));
    }

    @Test
    public void verifyAllowsOriginSafari() throws Exception {
        assertTrue("Chrome Extension Origin must be allowed", corsDefaults.allowsOrigin("safari-extension://com.atlassian.bonfire-upxu4cqz5p"));
    }

    @Test
    public void verifyDisallowsOriginHttp() throws Exception {
        assertFalse("HTTP origin must not be allowed", corsDefaults.allowsOrigin("http://cors-request-origin"));
    }

    @Test
    public void verifyNullOriginDontThrow() throws Exception {
        assertFalse("null origin must not be allowed, neither should throw", corsDefaults.allowsOrigin(null));
    }
}