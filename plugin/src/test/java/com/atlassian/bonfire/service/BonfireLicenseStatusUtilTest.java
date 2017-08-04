package com.atlassian.bonfire.service;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;

import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

public class BonfireLicenseStatusUtilTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PluginLicense pluginLicense;

    @Test
    public void verifyValidLicenseWithExpiredMaintenanceIsConvertedToActivated() throws Exception {
        when(pluginLicense.isValid()).thenReturn(true);
        when(pluginLicense.isMaintenanceExpired()).thenReturn(true);


        BonfireLicenseService.Status convert = BonfireLicenseStatusUtil.convert(pluginLicense);

        Assert.assertEquals(BonfireLicenseService.Status.activated, convert);
    }

    @Test
    public void verifyInvalidLicenseWithExpiredMaintenanceIsConvertedToMaintenanceExpired() throws Exception {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.getError()).thenReturn(Option.some(LicenseError.VERSION_MISMATCH));

        BonfireLicenseService.Status convert = BonfireLicenseStatusUtil.convert(pluginLicense);

        Assert.assertEquals(BonfireLicenseService.Status.maintenanceExpired, convert);
    }

    @Test
    public void verifyInvalidExpiredLicenseIsConvertedToExpired() throws Exception {
        when(pluginLicense.isValid()).thenReturn(false);
        when(pluginLicense.isEvaluation()).thenReturn(false);
        when(pluginLicense.isActive()).thenReturn(true);
        when(pluginLicense.getError()).thenReturn(Option.some(LicenseError.EXPIRED));

        BonfireLicenseService.Status convert = BonfireLicenseStatusUtil.convert(pluginLicense);

        Assert.assertEquals(BonfireLicenseService.Status.expired, convert);
    }
}