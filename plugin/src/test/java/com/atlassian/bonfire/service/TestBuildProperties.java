package com.atlassian.bonfire.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class TestBuildProperties {
    BuildPropertiesService buildPropertiesService;

    @Before
    public void setUp() {
        buildPropertiesService = new BuildPropertiesServiceImpl();
    }

    @Test
    public void testBuildDateReplaced() {
        assertNotNull(buildPropertiesService.getBuildDate());
        assertTrue(buildPropertiesService.getBuildDate().isBeforeNow());
    }

    @Test
    public void testVersionReplaced() {
        assertFalse("Version Replaced", buildPropertiesService.getVersion().equals("@@build.version@@"));
    }

}
