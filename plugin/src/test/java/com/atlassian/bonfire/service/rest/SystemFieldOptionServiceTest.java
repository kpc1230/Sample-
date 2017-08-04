package com.atlassian.bonfire.service.rest;

import com.atlassian.bonfire.rest.model.FieldOptionBean;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemFieldOptionServiceTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private ConstantsManager jiraConstantsManager;

    @Mock
    private VersionManager jiraVersionManager;

    @Mock
    private IssueSecurityLevelManager jiraIssueSecurityLevelManager;

    @AvailableInContainer
    @Mock
    private IssueSecurityLevelManager issueSecurityLevelManager;

    @InjectMocks
    private SystemFieldOptionService service = new SystemFieldOptionService();

    @Mock
    private ApplicationUser user;

    @Test
    public void testGetOptionsEmpty() throws GenericEntityException {
        String typeKey = "invalid.type";
        Project project = mock(Project.class);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
    }

    @Test
    public void testGetPriorityOptionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.priority";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
        // Does run
        verify(jiraConstantsManager).getPriorityObjects();
    }

    @Test
    public void testGetPriorityOptions() throws GenericEntityException {
        String typeKey = "issue.field.priority";
        Project project = mock(Project.class);
        Priority priorityOne = mock(Priority.class);
        Priority priorityTwo = mock(Priority.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(jiraConstantsManager.getPriorityObjects()).thenReturn(Lists.newArrayList(priorityOne, priorityTwo));

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
    }

    @Test
    public void testGetResolutionOptionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.resolution";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
        // Does run
        verify(jiraConstantsManager).getResolutionObjects();
    }

    @Test
    public void testGetResolutionOptions() throws GenericEntityException {
        String typeKey = "issue.field.resolution";
        Project project = mock(Project.class);
        Resolution resolutionOne = mock(Resolution.class);
        Resolution resolutionTwo = mock(Resolution.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(jiraConstantsManager.getResolutionObjects()).thenReturn(Lists.newArrayList(resolutionOne, resolutionTwo));

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
        // Does run
        verify(jiraConstantsManager).getResolutionObjects();
    }

    @Test
    public void testGetSecurityLevelOptionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.securitylevel";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
        // Does run
        verify(issueSecurityLevelManager).getUsersSecurityLevels(project, user);
    }

    @Test
    public void testGetSecurityLevelOptions() throws GenericEntityException {
        String typeKey = "issue.field.securitylevel";
        Project project = mock(Project.class);
        IssueSecurityLevel securityLevelOne = mock(IssueSecurityLevel.class);
        IssueSecurityLevel securityLevelTwo = mock(IssueSecurityLevel.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(issueSecurityLevelManager.getUsersSecurityLevels(project, user)).thenReturn(Lists.newArrayList(securityLevelOne, securityLevelTwo));
        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
        // Does run
        verify(issueSecurityLevelManager).getUsersSecurityLevels(project, user);
    }


    @Test
    public void testGetComponentsOptionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.components";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
        // Does run
        verify(project).getProjectComponents();
    }

    @Test
    public void testGetComponentsOptions() throws GenericEntityException {
        String typeKey = "issue.field.components";
        Project project = mock(Project.class);
        ProjectComponent compOne = mock(ProjectComponent.class);
        ProjectComponent compTwo = mock(ProjectComponent.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(project.getProjectComponents()).thenReturn(Lists.newArrayList(compOne, compTwo));

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
        // Does run
        verify(project).getProjectComponents();
    }

    @Test
    public void testGetAffectVersionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.affectsversions";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
    }

    @Test
    public void testGetAffectVersions() throws GenericEntityException {
        String typeKey = "issue.field.affectsversions";
        Project project = mock(Project.class);
        Version versionOne = mock(Version.class);
        Version versionTwo = mock(Version.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(jiraVersionManager.getVersionsUnreleased(id, false)).thenReturn(Lists.newArrayList(versionOne, versionTwo));

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
    }

    @Test
    public void testGetFixVersionsEmpty() throws GenericEntityException {
        String typeKey = "issue.field.fixversions";
        Project project = mock(Project.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.isEmpty());
    }

    @Test
    public void testGetFixVersions() throws GenericEntityException {
        String typeKey = "issue.field.fixversions";
        Project project = mock(Project.class);
        Version versionOne = mock(Version.class);
        Version versionTwo = mock(Version.class);
        Long id = 10000l;

        when(project.getId()).thenReturn(id);
        when(jiraVersionManager.getVersionsUnreleased(id, false)).thenReturn(Lists.newArrayList(versionOne, versionTwo));

        List<FieldOptionBean> options = service.getOptions(typeKey, project, user);

        assertTrue(options.size() == 2);
    }

    @Test
    public void testGetDefaultValueNoDefault() {
        OrderableField field = mock(OrderableField.class);
        Issue issue = mock(Issue.class);

        when(field.getDefaultValue(issue)).thenReturn(null);

        String defaultValue = service.getDefaultValue(field, issue);

        assertTrue(defaultValue == null);
    }

    @Test
    public void testGetDefaultValueNotSupportedField() {
        OrderableField field = mock(OrderableField.class);
        Issue issue = mock(Issue.class);
        Object notNull = mock(Object.class);

        when(field.getDefaultValue(issue)).thenReturn(notNull);
        // Default values cannot be set on affects versions...yet
        when(field.getNameKey()).thenReturn("issue.field.affectsversions");

        String defaultValue = service.getDefaultValue(field, issue);

        assertTrue(defaultValue == null);
    }

    @Test
    public void testGetDefaultValuePriority() {
        OrderableField field = mock(OrderableField.class);
        Issue issue = mock(Issue.class);
        IssueConstantImpl priority = mock(IssueConstantImpl.class);

        when(field.getDefaultValue(issue)).thenReturn(priority);
        when(priority.getString("name")).thenReturn("Name");
        when(field.getNameKey()).thenReturn("issue.field.priority");
        when(priority.getId()).thenReturn("Id");

        String defaultValue = service.getDefaultValue(field, issue);

        assertTrue(defaultValue != null);
        assertTrue(defaultValue.equals("Id"));
    }

    @Test
    public void testGetDefaultValueSecurityLevel() {
        OrderableField field = mock(OrderableField.class);
        MockProject project = new MockProject(1000L, "PRO");
        MockIssue issue = new MockIssue(2000, "PRO-1");
        issue.setProjectObject(project);

        when(issueSecurityLevelManager.getDefaultSecurityLevel(any(Project.class))).thenReturn(10010L);
        when(field.getNameKey()).thenReturn("issue.field.securitylevel");

        // Even though we don't use this value, we still need it to prevent the null check from failing
        when(field.getDefaultValue(issue)).thenReturn(mock(GenericValue.class));

        String defaultValue = service.getDefaultValue(field, issue);
        assertEquals("10010", defaultValue);
    }
}
