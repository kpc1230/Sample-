package com.atlassian.borrowed.greenhopper.customfield;

import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

@Service(CustomFieldService.SERVICE)
public class CustomFieldServiceImpl implements CustomFieldService {
    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource
    private CustomFieldManager customFieldManager;

    @Resource
    private FieldScreenManager fieldScreenManager;

    @JIRAResource
    private JiraContextTreeManager jiraContextTreeManager;

    @JIRAResource
    private IssueIndexManager reindexer;

    @Resource
    private ConstantsManager constantsManager;

    public CustomField createCustomField(CustomFieldMetadata fieldMetadata) {
        String name = i18n.getText(fieldMetadata.getFieldName());
        String desc = i18n.getText(fieldMetadata.getFieldDescription());
        CustomFieldType type = customFieldManager.getCustomFieldType(fieldMetadata.getFieldType());
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(fieldMetadata.getFieldSearcher());
        List<IssueType> issueTypes = buildIssueTypes(fieldMetadata.getIssueTypes());

        // we use the global context here (all projects), since we're creating a field programmatically and don't really want anything project-specific
        List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, null, null, jiraContextTreeManager);

        try {
            return customFieldManager.createCustomField(name, desc, type, searcher, contexts, issueTypes);

        } catch (Exception e) {
            throw new CustomFieldException("Exception while trying to create a customField with the following parameters: " + fieldMetadata, e);
        }
    }

    public CustomField getCustomField(Long id) {
        return customFieldManager.getCustomFieldObject(id);
    }

    public void reindexSingleIssue(Issue issue) throws IndexException {
        reindexer.reIndex(issue);
    }

    /**
     * Associate the custom field with the default screen.
     */
    public void associateWithDefaultScreen(CustomField customField) {
        notNull(customField, "The custom field to associate with the default screen cannot be null");

        // fetch the default screen
        FieldScreen defaultScreen = fieldScreenManager.getFieldScreen(FieldScreen.DEFAULT_SCREEN_ID);

        // check whether the field has already been added to the screen (regardless of what tab)
        if (!defaultScreen.containsField(customField.getId())) {
            // just add the field to the first tab
            // JIRA uses a List internally, so the tag position is simply the index (which starts at 0)
            FieldScreenTab firstTab = defaultScreen.getTab(0);
            firstTab.addFieldScreenLayoutItem(customField.getId());
        }
    }

    private List<IssueType> buildIssueTypes(String[] ids) {
        final ArrayList<IssueType> issueTypes = Lists.newArrayList();
        for (String id : ids) {
            if ("-1".equals(id)) {
                issueTypes.add(null);
            } else {
                constantsManager.getIssueTypeObject(id);
            }
        }
        return issueTypes;
    }

}
