package com.atlassian.bonfire.upgradetasks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import com.atlassian.jira.database.QueryDslAccessor;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.task.progress.ProgressUpdateWriter;
import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.customfield.TestingStatusCustomFieldService;
import com.atlassian.bonfire.service.TestingStatusService;

import static com.atlassian.bonfire.service.TestingStatusService.TestingStatus.IN_PROGRESS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdHocTestingStatusDuplicatesRemovalUpgradeTaskTest {

    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private JiraFeatureFlagService featureFlagService;

    @Mock
    private QueryDslAccessor queryDslAccessor;

    @Mock
    private TestingStatusService testingStatusService;

    @Mock
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Mock
    private BonfireMultiSessionCustomFieldService multiSessionCustomFieldService;

    @Mock
    private IssueManager jiraIssueManager;

    @Mock
    private MutableIssue issue;

    @Mock
    private CustomField testingStatusCF;

    @Mock
    private CustomField multiSessionCF;

    @Mock
    private ProgressUpdateWriter taskProgressWriter;

    @Mock
    private CustomFieldType customFieldType;

    @InjectMocks
    private AdHocTestingStatusDuplicatesRemovalUpgradeTask upgradeTask;


    @Before
    public void setUp() throws Exception {

        List<Long> issuesWithDuplicatedValues = LongStream.range(10024L, 10100L).boxed().collect(Collectors.toList());


        when(multiSessionCF.getIdAsLong()).thenReturn(100L);


        when(queryDslAccessor.executeQuery(Mockito.any()))
                .thenReturn(issuesWithDuplicatedValues);

        when(testingStatusCustomFieldService.getTestingStatusSessionCustomField()).thenReturn(testingStatusCF);
        when(multiSessionCustomFieldService.getRelatedToSessionCustomField()).thenReturn(multiSessionCF);

        when(testingStatusCF.getCustomFieldType()).thenReturn(customFieldType);
        when(testingStatusService.calculateTestingStatus(any())).thenReturn(IN_PROGRESS);
        when(jiraIssueManager.getIssueObject(Mockito.anyLong())).thenReturn(issue);
    }

    @Test
    public void testRemovingTestingStatusDuplicatedValues() throws Exception {
        upgradeTask.runUpgradeTask(taskProgressWriter);

        verify(testingStatusService, times(76)).calculateTestingStatus(issue);
        verify(customFieldType, times(76))
                .updateValue(testingStatusCF, issue, IN_PROGRESS.getI18nKey());
    }
}