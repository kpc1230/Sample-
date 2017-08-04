package com.atlassian.bonfire.service;

import com.atlassian.bonfire.service.BonfireLicenseService.Status;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TestBonfireLicenseServiceImpl {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PluginLicenseManager pluginLicenseManager;

    @Mock
    private PluginLicense pluginLicense;

    @InjectMocks
    private BonfireLicenseServiceImpl service = new BonfireLicenseServiceImpl();

    @Before
    public void init() {
        when(pluginLicense.isValid()).thenReturn(true);
        when(pluginLicenseManager.getLicense()).thenReturn(Option.some(pluginLicense));
    }

    /**
     * All expiry dates used for licenses won't expire until the year 2200. If you are fixing these tests in that year: you will need a new license
     * with a new expiry date.
     */

    @Test
    public void testValidLicense() {
        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.activated, status);
    }

    // This test case would only happen in studio
    @Test
    public void testExpiredLicense() {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.isActive()).thenReturn(true);
        when(pluginLicense.getError()).thenReturn(Option.some(LicenseError.EXPIRED));

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.expired, status);
    }

    @Test
    public void testExpiredEvalLicense() {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.isActive()).thenReturn(true);
        when(pluginLicense.isEvaluation()).thenReturn(true);
        when(pluginLicense.getError()).thenReturn(Option.some(LicenseError.EXPIRED));

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.evalExpired, status);
    }

    @Test
    public void verifyValidButMaintenanceExpiredLicenseIsStillActive() {
        when(pluginLicense.isMaintenanceExpired()).thenReturn(true);

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.activated, status);
    }

    @Test
    public void testValidEvalLicense() {
        when(pluginLicense.isEvaluation()).thenReturn(true);

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.activated, status);
    }

    @Test(expected = NullPointerException.class)
    public void testNullLicense() {
        when(pluginLicenseManager.getLicense()).thenReturn(null);
        Status status = service.getLicenseStatus();
    }

    @Test
    public void testEmptyLicense() {
        when(pluginLicenseManager.getLicense()).thenReturn(Option.<PluginLicense>none());

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.unactivated, status);
    }

    @Test
    public void testInactiveLicense() {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.isActive()).thenReturn(false);
        when(pluginLicense.getError()).thenReturn(Option.some(LicenseError.EXPIRED));

        Status status = service.getLicenseStatus();
        assertEquals("License status are not the same", Status.unactivated, status);
    }
}
