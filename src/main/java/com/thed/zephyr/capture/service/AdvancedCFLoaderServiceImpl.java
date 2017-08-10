package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.issue.CreateIssue;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service(AdvancedCFLoaderService.SERVICE)
public class AdvancedCFLoaderServiceImpl implements AdvancedCFLoaderService {

    @JIRAResource
    private IssueFactory jiraIssueFactory;

    @JIRAResource
    private IssueCreationHelperBean issueCreationHelperBean;

    @JIRAResource
    private ProjectFactory projectFactory;

    @JIRAResource
    private CustomFieldManager jiraCustomFieldManager;

    @Resource(name = BonfireSupportedFieldService.SERVICE)
    private BonfireSupportedFieldService bonfireSupportedFieldService;

    public List<String> getAdvancedCustomFields(ApplicationUser user, Project project, IssueType issueType) {
        MutableIssue dummy = getIssueObject(project, issueType);
        CreateIssue createBean = new CreateIssue(jiraIssueFactory, issueCreationHelperBean);
        FieldScreenRenderer createIssueScreen = issueCreationHelperBean.createFieldScreenRenderer(dummy);
        List<String> toReturn = Lists.newArrayList();
        for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : createIssueScreen.getAllScreenRenderItems()) {
            OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();
            if (!isFieldSupported(orderableField, project, issueType, user, fieldScreenRenderLayoutItem)) {
                // Populate default values
                fieldScreenRenderLayoutItem.populateDefaults(createBean.getFieldValuesHolder(), dummy);

                String html = fieldScreenRenderLayoutItem.getCreateHtml(createBean, createBean, dummy, getDisplayParams());
                if (StringUtils.isNotBlank(html)) {
                    toReturn.add(html);
                }
            }
        }
        return toReturn;
    }

    private boolean isFieldSupported(OrderableField orderableField, Project project, IssueType issueType, ApplicationUser user, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        CustomField customField = jiraCustomFieldManager.getCustomFieldObject(orderableField.getId());
        // If it is not a custom field then it is a system field
        final boolean fieldSupported;
        if (customField == null) {
            fieldSupported = bonfireSupportedFieldService.isFieldSupported(orderableField.getNameKey());
        } else {
            fieldSupported = bonfireSupportedFieldService.isFieldSupported(customField.getCustomFieldType().getKey());
        }
        return fieldSupported || bonfireSupportedFieldService.isAdvancedCustomFieldSupported(project, issueType, fieldScreenRenderLayoutItem);
    }

    /*
     * The issue that gets returned here is simply a wrapper for the project and projectId. JIRA does it too.
     */
    private MutableIssue getIssueObject(Project p, IssueType t) {
        MutableIssue issueObject = jiraIssueFactory.getIssue();
        issueObject.setProjectId(p.getId());
        issueObject.setIssueTypeId(t.getId());

        return issueObject;
    }

    /**
     * Display parameters that are needed to properly render some fields
     */
    private Map<String, Object> getDisplayParams() {
        final Map<String, Object> displayParams = Maps.newHashMap();
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
