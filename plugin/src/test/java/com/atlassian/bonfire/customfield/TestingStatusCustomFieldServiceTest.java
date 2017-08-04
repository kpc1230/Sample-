package com.atlassian.bonfire.customfield;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.atlassian.borrowed.greenhopper.customfield.CustomFieldService;
import com.atlassian.excalibur.service.dao.PropertyDao;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;

import com.atlassian.bonfire.features.CaptureFeatureFlags;
import com.atlassian.bonfire.service.TestingStatusService;

import static com.atlassian.bonfire.service.TestingStatusService.TestingStatus.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestingStatusCustomFieldServiceTest {

    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private JiraFeatureFlagService jiraFeatureFlagService;

    @Mock
    private CustomFieldService customFieldService;

    @Mock
    private TestingStatusService testingStatusService;

    @InjectMocks
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Mock
    private PropertyDao propertyDao;

    @Mock
    private CustomField customField;

    @Mock
    private CustomFieldType customFieldType;

    @Mock
    private Issue issue;

    @Before
    public void setUp() throws Exception {
        when(issue.getId()).thenReturn(1l);
        when(customField.getId()).thenReturn("1");
        when(propertyDao.getLongProperty(anyString())).thenReturn(1l);
        when(customFieldService.getCustomField(anyLong())).thenReturn(customField);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);
        when(testingStatusService.calculateTestingStatus(any())).thenReturn(IN_PROGRESS);
    }

    @Test
    public void testThatValuesIsUpdatedInDBWhenFeatureFlagIsEnabled() throws Exception {
        when(jiraFeatureFlagService.isEnabled(CaptureFeatureFlags.TESTING_STATUS_UPDATE_IN_DB.asFlag())).thenReturn(true);

        testingStatusCustomFieldService.updateTestingStatus(issue);
        verify(customFieldType, times(1)).updateValue(any(), any(), anyString());
    }

    @Test
    public void testThatValuesIsNotUpdatedInDBWhenFeatureFlagIsDisabled() throws Exception {
        when(jiraFeatureFlagService.isEnabled(CaptureFeatureFlags.TESTING_STATUS_UPDATE_IN_DB.asFlag())).thenReturn(false);

        testingStatusCustomFieldService.updateTestingStatus(issue);

        verify(customFieldType, never()).updateValue(any(), any(), anyString());
    }
}