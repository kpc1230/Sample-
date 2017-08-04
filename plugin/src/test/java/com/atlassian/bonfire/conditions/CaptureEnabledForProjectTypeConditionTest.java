package com.atlassian.bonfire.conditions;

import com.atlassian.bonfire.service.CaptureAdminSettingsService;
import com.atlassian.bonfire.service.ProjectTypeService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.MockApplicationUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CaptureEnabledForProjectTypeConditionTest {
    private static final String PROJECT = "project";
    private static final String CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES = "capture.show.for.others.project.types";
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);
    @Mock
    private CaptureAdminSettingsService captureAdminSettingsService;
    @Mock
    private ProjectTypeService projectTypeService;
    @Mock
    private FeatureManager featureManager;
    @InjectMocks
    private CaptureEnabledForProjectTypeCondition condition = new CaptureEnabledForProjectTypeCondition();

    private Map<String, Object> contextMap = new HashMap<>();
    private MockProject project = new MockProject(1001L, "DEMO");
    private MockApplicationUser applicationUser = new MockApplicationUser("charlie");

    @Before
    public void setUp() throws Exception {
        contextMap.put(PROJECT, project);
        when(projectTypeService.isProjectTypesSupported()).thenReturn(true);
    }

    @Test
    public void verifyHideProjectKeyIsNull() throws Exception {
        contextMap.put(PROJECT, null);
        assertFalse("No Capture link when project key is undefined", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyShowProjectTypesAreNotSupported() throws Exception {
        when(projectTypeService.isProjectTypesSupported()).thenReturn(false);
        assertTrue("We should always show toolbar in JIRA 6.4 and earlier", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyShowWhenServiceDeskProjectAndEnabled() {
        when(captureAdminSettingsService.isServiceDeskProjectsEnabled()).thenReturn(true);
        when(projectTypeService.isServiceDeskProject(eq(project))).thenReturn(true);

        assertTrue("Show toolbar for the Service Desk project when Service Desk project is enabled in Configuration", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyHideWhenServiceDeskProjectAndDisabled() {
        when(captureAdminSettingsService.isServiceDeskProjectsEnabled()).thenReturn(false);
        when(projectTypeService.isServiceDeskProject(eq(project))).thenReturn(true);

        assertFalse("Hide toolbar for the Service Desk project when Service Desk project is disabled in Configuration", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyShowWhenBusinessProjectAndEnabled() {
        when(captureAdminSettingsService.isBusinessProjectsEnabled()).thenReturn(true);
        when(projectTypeService.isBusinessProject(eq(project))).thenReturn(true);

        assertTrue("Show toolbar for the Business project when Business project is enabled in Configuration", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyHideWhenBusinessProjectAndDisabled() {
        when(captureAdminSettingsService.isBusinessProjectsEnabled()).thenReturn(false);
        when(projectTypeService.isBusinessProject(eq(project))).thenReturn(true);

        assertFalse("Hide toolbar for the Business project when Business project is disabled in Configuration", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyHideWhenProjectTypeUnknownAndNoDarkFeatureDisabled() {
        when(projectTypeService.isServiceDeskProject(eq(project))).thenReturn(false);
        when(projectTypeService.isBusinessProject(eq(project))).thenReturn(false);
        when(projectTypeService.isSoftwareProject(eq(project))).thenReturn(false);
        when(featureManager.isEnabled(eq(CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES))).thenReturn(false);
        assertFalse("Hide toolbar for the unknown project type when Dark Feature is not set", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyShowWhenProjectTypeUnknownAndNoDarkFeatureEnabled() {
        when(projectTypeService.isServiceDeskProject(eq(project))).thenReturn(false);
        when(projectTypeService.isBusinessProject(eq(project))).thenReturn(false);
        when(projectTypeService.isSoftwareProject(eq(project))).thenReturn(false);
        when(featureManager.isEnabled(eq(CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES))).thenReturn(true);
        assertTrue("Show toolbar for the unknown project type when Dark Feature is set", condition.shouldDisplay(contextMap));
    }

    @Test
    public void verifyShowWhenProjectTypeUndefined() {
        when(projectTypeService.isProjectTypeUndefined(eq(project))).thenReturn(true);
        assertTrue("Show toolbar for the undefined project type", condition.shouldDisplay(contextMap));
    }
}