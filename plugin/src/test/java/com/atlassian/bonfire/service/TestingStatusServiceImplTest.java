package com.atlassian.bonfire.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.bonfire.customfield.BonfireContextCustomFieldsService;
import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.customfield.BonfireSessionCustomFieldService;
import com.atlassian.bonfire.customfield.TestingStatusCustomFieldService;
import com.atlassian.bonfire.model.LightSession;

import static com.atlassian.bonfire.service.TestingStatusService.TestingStatus;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestingStatusServiceImplTest {

    private String notStartedI18nKey = TestingStatus.NOT_STARTED.getI18nKey();

    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Mock
    private SessionController sessionController;

    @Mock
    private BonfireI18nService i18n;

    @Mock
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Mock
    private JiraFeatureFlagService jiraFeatureFlagService;

    @Mock
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Mock
    private BonfireContextCustomFieldsService bonfireContextCustomFieldsService;

    @InjectMocks
    private TestingStatusServiceImpl testingStatusService = new TestingStatusServiceImpl();

    @Mock
    private Issue issue;

    @Mock
    private CustomField relatedToCustomField;

    @Mock
    private CustomField raisedInCustomField;

    @Mock
    private CustomField testingStatusCustomField;

    @Mock
    private CustomFieldType customFieldType;

    @Mock
    private LightSession session;

    @Before
    public void setUp() throws Exception {
        when(testingStatusCustomFieldService.getTestingStatusSessionCustomField()).thenReturn(testingStatusCustomField);
        when(jiraFeatureFlagService.isEnabled(any())).thenReturn(true);
        when(bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField()).thenReturn(relatedToCustomField);
        when(relatedToCustomField.getValueFromIssue(issue)).thenReturn("");
        when(bonfireSessionCustomFieldService.getRaisedInSessionCustomField()).thenReturn(raisedInCustomField);
        when(testingStatusCustomField.getCustomFieldType()).thenReturn(customFieldType);
    }

    @Test
    public void testTestingStatusFoundInDB() throws Exception {
        when(testingStatusCustomField.getValueFromIssue(issue)).thenReturn(TestingStatus.IN_PROGRESS.getI18nKey());

        testingStatusService.getTestingStatus(issue);

        verify(bonfireMultiSessionCustomFieldService, times(0)).getRelatedToSessionCustomField();
        verify(customFieldType, times(0))
                .updateValue(any(), any(), anyString());
    }

    //Group of tests that make sure that when TestingStatus for issue will not be found in DB Capture will add it if needed (if issue has value for any Capture custom field)

    @Test
    public void testTestingStatusForIssueWithAllCaptureCustomFieldsValuesNotFoundInDB() throws Exception {
        setUpMocksforHasAnyCaptureCustomFieldValueMethod(true, true, true);

        testingStatusService.getTestingStatus(issue);

        verify(customFieldType, times(1))
                .updateValue(testingStatusCustomField, issue, notStartedI18nKey);
    }

    @Test
    public void testTestingStatusForIssueWithRaisedInCustomFieldsValueNotFoundInDB() throws Exception {
        setUpMocksforHasAnyCaptureCustomFieldValueMethod(false, true, false);

        testingStatusService.getTestingStatus(issue);

        verify(customFieldType, times(1))
                .updateValue(testingStatusCustomField, issue, TestingStatus.NOT_STARTED.getI18nKey());
    }

    @Test
    public void testTestingStatusForIssueWithContextValuesNotFoundInDB() throws Exception {
        setUpMocksforHasAnyCaptureCustomFieldValueMethod(false, false, true);

        testingStatusService.getTestingStatus(issue);

        verify(bonfireContextCustomFieldsService, times(1)).hasContextValues(issue);
        verify(customFieldType, times(1))
                .updateValue(testingStatusCustomField, issue, TestingStatus.NOT_STARTED.getI18nKey());
    }

    @Test
    public void testTestingStatusForIssueWithNoCaptureCustomFieldsValuesNotFoundInDB() throws Exception {
        setUpMocksforHasAnyCaptureCustomFieldValueMethod(false, false, false);

        testingStatusService.getTestingStatus(issue);

        verify(customFieldType, times(0))
                .updateValue(any(), any(), anyString());
    }

    private void setUpMocksforHasAnyCaptureCustomFieldValueMethod(boolean hasRelatedToValue, boolean hasRaisedInValue, boolean hasContextValues) {
        when(relatedToCustomField.hasValue(issue)).thenReturn(hasRelatedToValue);
        when(raisedInCustomField.hasValue(issue)).thenReturn(hasRaisedInValue);
        when(bonfireContextCustomFieldsService.hasContextValues(issue)).thenReturn(hasContextValues);
    }
}