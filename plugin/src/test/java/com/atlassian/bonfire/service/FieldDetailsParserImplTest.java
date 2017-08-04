package com.atlassian.bonfire.service;

import com.atlassian.bonfire.service.parser.FieldDetailsParserImpl;
import com.atlassian.bonfire.service.parser.ParsedField;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.MockOrderableField;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.BulkFieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserIssueHistoryManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import webwork.action.Action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class FieldDetailsParserImplTest {
    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private BulkFieldScreenRenderLayoutItemImpl fieldScreenRenderLayoutItem;
    @Mock
    private IssueFactory jiraIssueFactory;

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;

    @Mock
    @AvailableInContainer
    private CustomFieldManager customFieldManager;

    @Mock
    @AvailableInContainer
    private AttachmentManager attachmentManager;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    @AvailableInContainer
    private VersionManager versionManager;

    @Mock
    @AvailableInContainer
    private UserIssueHistoryManager userIssueHistoryManager;

    @Mock
    @AvailableInContainer
    private TimeTrackingConfiguration timeTrackingConfiguration;

    private Project project;
    private MockIssueType issueType;

    private FieldDetailsParserImpl fieldDetailsParser;
    private MockIssue issue;
    private MockFieldLayoutItem fieldLayoutItem;


    @Before
    public void setUp() throws Exception {
        project = new MockProject();
        issueType = new MockIssueType("1", "Feature");
        issue = new MockIssue();
        fieldDetailsParser = new FieldDetailsParserImpl();
        fieldDetailsParser.setJiraIssueFactory(jiraIssueFactory);
        fieldLayoutItem = new MockFieldLayoutItem();

        when(jiraIssueFactory.getIssue()).thenReturn(issue);
        when(fieldScreenRenderLayoutItem.getFieldLayoutItem()).thenReturn(fieldLayoutItem);
    }

    @Test
    public void testParseTempoAccountSelectNormalOptions() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("<div class=\"field-group\" >\n" +
                "                                                                        <label for=\"customfield_10100\">Account</label>\n" +
                "                    \n" +
                "<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Super Luigi (SUPERLUIGI)\n" +
                "        </option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n" +
                "\n" +
                "                                                   <div class=\"description\">Tempo Account custom field</div>\n" +
                "                </div>\n" +
                "        \n" +
                "<script type=\"text/javascript\">\n" +
                "    function setAccountPicker() {\n" +
                "        var field = jQuery(\"#customfield_10100-field\");\n" +
                "        if (field.val() == undefined) {\n" +
                "            new AJS.SingleSelect({ element: jQuery(\"#customfield_10100\") })\n" +
                "        }\n" +
                "    }\n" +
                "    jQuery(document).ready(function () {\n" +
                "        setAccountPicker();\n" +
                "    });\n" +
                "    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {\n" +
                "        setAccountPicker();\n" +
                "    });\n" +
                "\n" +
                "</script>");
        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNotNull("Select field must be parsed properly", parsedField);
        assertEquals(2, parsedField.getOptions().size());
        assertEquals("1", parsedField.getOptions().get(0).getValue());
        assertEquals("2", parsedField.getOptions().get(1).getValue());
    }

    @Test
    public void testParseTempoAccountSelectOptGroupOptions() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("                                            <div class=\"field-group\" >\n" +
                "                                                                        <label for=\"customfield_10100\">Account</label>\n" +
                "                    \n" +
                "<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "            <option value=\"3\" >\n" +
                "            Earth (EARTH)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Mercury (MERCURY)\n" +
                "        </option>\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n" +
                "\n" +
                "                                                   <div class=\"description\">Tempo Account custom field</div>\n" +
                "                </div>\n" +
                "        \n" +
                "<script type=\"text/javascript\">\n" +
                "    function setAccountPicker() {\n" +
                "        var field = jQuery(\"#customfield_10100-field\");\n" +
                "        if (field.val() == undefined) {\n" +
                "            new AJS.SingleSelect({ element: jQuery(\"#customfield_10100\") })\n" +
                "        }\n" +
                "    }\n" +
                "    jQuery(document).ready(function () {\n" +
                "        setAccountPicker();\n" +
                "    });\n" +
                "    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {\n" +
                "        setAccountPicker();\n" +
                "    });\n" +
                "\n" +
                "</script>\n");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNotNull("Select field must be parsed properly", parsedField);
        assertEquals(2, parsedField.getOptions().size());
        assertEquals("Earth (EARTH)", parsedField.getOptions().get(0).getText());
        assertEquals("Mercury (MERCURY)", parsedField.getOptions().get(1).getText());
    }

    @Test
    public void testParseTempoAccountOnlySelect() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Super Luigi (SUPERLUIGI)\n" +
                "        </option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNotNull("Select field must be parsed properly", parsedField);
        assertEquals(2, parsedField.getOptions().size());
        assertEquals("1", parsedField.getOptions().get(0).getValue());
        assertEquals("2", parsedField.getOptions().get(1).getValue());
    }

    @Test
    public void testParseTempoAccountFaultyHTML() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("                                                <div class=\"field-group\" >\n" +
                "                                                                        <label for=\"customfield_10100\">Account</label>\n" +
                "                    \n" +
                "<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNotNull("Select field must be parsed properly", parsedField);
        assertEquals(1, parsedField.getOptions().size());
        assertEquals("1", parsedField.getOptions().get(0).getValue());

    }

    @Test
    public void testNoParseTempoAccountNullHtml() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn(null);

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNull("Null Html. Field must not be parsed.", parsedField);

    }

    @Test
    public void testNoParseTempoAccountSelectWrongFieldId() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_WRONG_ID"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Super Luigi (SUPERLUIGI)\n" +
                "        </option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNull("Field ID does not match layout item field id. Field must not be parsed.", parsedField);
    }

    @Test
    public void testNoParseTempoAccountTwoSelectElements() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10100"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Super Luigi (SUPERLUIGI)\n" +
                "        </option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n" +
                "<select id=\"customfield_10100\" name=\"customfield_10100\" class=\"customfield_10100 select\">\n" +
                "    <option value=\"\">Please select</option>\n" +
                "            <option value=\"1\" >\n" +
                "            Pink Leopard (PINKLEOPARD)\n" +
                "        </option>\n" +
                "            <option value=\"2\" >\n" +
                "            Super Luigi (SUPERLUIGI)\n" +
                "        </option>\n" +
                "        <optgroup id=\"tempo-report-activity-suggested\" label=\"Global\" data-weight=\"0\">\n" +
                "        </optgroup>\n" +
                "\n" +
                "</select>\n");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNull("Field ID does not match layout item field id. Field must not be parsed.", parsedField);
    }

    @Test
    public void testNoParseForJiraAgileSprintField() throws Exception {
        when(fieldScreenRenderLayoutItem.getOrderableField()).thenReturn(new MockOrderableField("customfield_10004"));
        when(fieldScreenRenderLayoutItem.getCreateHtml(Matchers.<Action>anyObject(), Mockito.<OperationContext>anyObject(), Mockito.<Issue>anyObject(),
                Mockito.anyMap())).thenReturn("<div class=\"field-group\" >\n" +
                "                                                                        <label for=\"customfield_10004\">Sprint</label>\n" +
                "                \n" +
                "        <div id=\"js-customfield_10004-ss-container\">\n" +
                "        <select class=\"single-select long-field hidden js-sprint-picker\" id=\"customfield_10004\" name=\"customfield_10004\" data-container-class=\"long-field\" data-saved-id=\"\" data-saved-state=\"\">\n" +
                "            <option selected=\"selected\" value=\"\">\n" +
                "                \n" +
                "            </option>\n" +
                "        </select>\n" +
                "    </div>\n" +
                "                                                   <div class=\"description\">JIRA Agile sprint field</div>\n" +
                "                </div>");

        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
        assertNull("Empty options are not supported. Field must not be parsed.", parsedField);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}