package com.atlassian.bonfire.service.rest;

import com.atlassian.bonfire.rest.model.FieldDetailsBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.BuildUtilsInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomFieldOptionServiceTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private BuildUtilsInfo jiraBuildUtilsInfo;

    @InjectMocks
    private CustomFieldOptionService service = new CustomFieldOptionService();

    @Test
    public void testGetDefaultValueNoDefault() {
        FieldDetailsBean bean = new FieldDetailsBean();
        CustomField customField = mock(CustomField.class);
        Issue issue = mock(Issue.class);

        when(customField.getDefaultValue(issue)).thenReturn(null);

        service.attachCustomFieldDefaultValue(bean, customField, issue);

        assertTrue("Options should not have a value", bean.getDefaultOptions() == null);
        assertTrue("String should not have a value", bean.getDefaultValueString() == null);
    }

    @Test
    public void testGetDefaultValueText() {
        FieldDetailsBean bean = new FieldDetailsBean();
        CustomField customField = mock(CustomField.class);
        Issue issue = mock(Issue.class);
        CustomFieldType cft = mock(CustomFieldType.class);
        Set<Label> defaultValue = new LinkedHashSet<Label>();
        defaultValue.add(new Label(1000l, 1000l, "Test"));
        defaultValue.add(new Label(1000l, 1000l, "ing"));

        when(customField.getDefaultValue(issue)).thenReturn(defaultValue);
        when(customField.getCustomFieldType()).thenReturn(cft);
        when(cft.getKey()).thenReturn("com.atlassian.jira.plugin.system.customfieldtypes:labels");

        service.attachCustomFieldDefaultValue(bean, customField, issue);

        assertTrue("Options should not have a value", bean.getDefaultOptions() == null);
        assertTrue("Default values do not match", bean.getDefaultValueString().equals("Test ing "));
    }
}
