package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.model.*;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfireSupportedFieldService;
import com.atlassian.bonfire.service.parser.FieldDetailsParser;
import com.atlassian.bonfire.service.parser.ParsedField;
import com.atlassian.bonfire.service.parser.ParsedFieldOption;
import com.atlassian.bonfire.service.rest.CustomFieldOptionService;
import com.atlassian.bonfire.service.rest.SystemFieldOptionService;
import com.atlassian.bonfire.service.rest.UserOptionService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Path("/fields")
public class FieldsResource extends BonfireRestResource {
    private static final String PROJECT_ID_OR_KEY = "projectIdOrKey";

    @Resource
    private CustomFieldManager jiraCustomFieldManager;

    @Resource
    private ProjectManager jiraProjectManager;

    @Resource
    private IssueTypeSchemeManager jiraIssueTypeSchemeManager;

    @Resource(name = SystemFieldOptionService.SERVICE)
    private SystemFieldOptionService systemFieldOptionService;

    @Resource(name = CustomFieldOptionService.SERVICE)
    private CustomFieldOptionService customFieldOptionService;

    @Resource(name = UserOptionService.SERVICE)
    private UserOptionService userOptionService;

    @Resource(name = BonfireSupportedFieldService.SERVICE)
    private BonfireSupportedFieldService bonfireSupportedFieldService;

    @Resource(name = FieldDetailsParser.SERVICE)
    private FieldDetailsParser fieldDetailsParser;

    @JIRAResource
    private IssueCreationHelperBean issueCreationHelperBean;

    private final IssueFactory jiraIssueFactory;

    public FieldsResource(final IssueFactory issueFactory) {
        super(FieldsResource.class);

        this.jiraIssueFactory = issueFactory;
    }

    /**
     * Returns a list of all the custom fields for a particular issueType in a particular project
     *
     * @param projectIdOrKey the project id or key to request
     * @return a Response containing a CustomFieldListBean
     */
    @GET
    @Path("/{projectIdOrKey}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFieldsForProjectFromPath(final @PathParam(PROJECT_ID_OR_KEY) String projectIdOrKey) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                ErrorCollection errorCollection = new ErrorCollection();

                Project project;
                try {
                    project = jiraProjectManager.getProjectObj(Long.parseLong(projectIdOrKey));
                } catch (NumberFormatException nfe) {
                    project = jiraProjectManager.getProjectObjByKey(projectIdOrKey);
                }
                if (project == null) {
                    return badRequest("fields.resource.project.not.found");
                }

                Collection<IssueType> issueTypes = jiraIssueTypeSchemeManager.getIssueTypesForProject(project);

                List<FieldListBean> fieldListBeans = new ArrayList<FieldListBean>(issueTypes.size());

                Map<String, FieldDetailsBean> allFieldsList = Maps.newHashMap();

                ArrayList<IssueType> subTasks = Lists.newArrayList();
                for (IssueType issueType : issueTypes) {
                    if (issueType.isSubTask()) {
                        subTasks.add(issueType);
                    } else {
                        // Standard Issue Types first
                        fieldListBeans.add(getFieldListBean(project, issueType, getLoggedInUser(), allFieldsList, errorCollection));
                    }
                }
                for (IssueType subTaskType : subTasks) {
                    // Then Sub Tasks
                    fieldListBeans.add(getFieldListBean(project, subTaskType, getLoggedInUser(), allFieldsList, errorCollection));
                }

                List<UserOptionBean> userList = userOptionService.getAssignableUsers(project);

                return errorCollection.hasErrors() ? badRequest(errorCollection) : ok(new FieldsResponse(fieldListBeans, allFieldsList, userList));
            }
        });
    }

    /*
     * Returns a FieldListBean that contains a set of Custom Fields and System Fields relating to the Project and IssueType
     */
    private FieldListBean getFieldListBean(Project project, IssueType issueType, ApplicationUser user, Map<String, FieldDetailsBean> allFieldsList,
                                           ErrorCollection errorCollection) {
        List<FieldBean> fields = Lists.newArrayList();

        // Get the issue creation scheme for the issue in this project
        FieldScreenRenderer createIssueScreen = issueCreationHelperBean.createFieldScreenRenderer(getIssueObject(project, issueType));

        // Loop through all the items(which are fields)
        int screenIndex = 0;
        int advFieldCount = 0;
        int requiredAdvFieldCount = 0;
        for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : createIssueScreen.getAllScreenRenderItems()) {
            OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

            if (orderableField.getNameKey().equals("issue.field.issuetype")) {
                // Don't include issuetypes in the field list beans.
                continue;
            }

            CustomField customField = jiraCustomFieldManager.getCustomFieldObject(orderableField.getId());
            // If it is not a custom field then it is a system field
            if (customField == null) {
                if (bonfireSupportedFieldService.isFieldSupported(orderableField.getNameKey())) {
                    String key = orderableField.getId();
                    if (!allFieldsList.containsKey(key)) {
                        FieldDetailsBean systemField = getSystemField(project, issueType, user, orderableField, fieldScreenRenderLayoutItem);
                        allFieldsList.put(key, systemField);
                    }
                    boolean isRequired = fieldScreenRenderLayoutItem.isRequired();

                    fields.add(new FieldBean(key, isRequired, screenIndex));
                } else {
                    advFieldCount++;
                    if (fieldScreenRenderLayoutItem.isRequired()) {
                        requiredAdvFieldCount++;
                    }
                }
            } else {
                final String key = customField.getId();
                final boolean isRequired = fieldScreenRenderLayoutItem.isRequired();
                if (bonfireSupportedFieldService.isFieldSupported(customField.getCustomFieldType().getKey())) {
                    if (!allFieldsList.containsKey(key)) {
                        // Create a custom field bean
                        FieldDetailsBean customFieldBean = getCustomFieldBean(customField.getId(), project, issueType, errorCollection,
                                fieldScreenRenderLayoutItem);
                        allFieldsList.put(key, customFieldBean);
                    }
                    fields.add(new FieldBean(key, isRequired, screenIndex));
                } else {
                    if (bonfireSupportedFieldService.isAdvancedCustomFieldSupported(project, issueType, fieldScreenRenderLayoutItem)) {
                        final ParsedField parsedField = fieldDetailsParser.parseFieldDetails(project, issueType, fieldScreenRenderLayoutItem);
                        if (!allFieldsList.containsKey(key)) {
                            // Create a custom field bean
                            FieldDetailsBean customFieldBean = toFieldDetails(parsedField);
                            allFieldsList.put(key, customFieldBean);
                        }
                        fields.add(new FieldBean(key, isRequired, screenIndex));
                    } else {
                        advFieldCount++;
                        if (fieldScreenRenderLayoutItem.isRequired()) {
                            requiredAdvFieldCount++;
                        }
                    }
                }
            }
            screenIndex++;
        }

        return new FieldListBean(fields, new IssueTypeBean(issueType), advFieldCount, requiredAdvFieldCount);
    }

    private FieldDetailsBean toFieldDetails(ParsedField parsedField) {
        final List<ParsedFieldOption> optionsDto = parsedField.getOptions();
        final List<FieldOptionBean> options = Lists.transform(optionsDto, new Function<ParsedFieldOption, FieldOptionBean>() {
            @Override
            public FieldOptionBean apply(ParsedFieldOption fieldOptionDto) {
                return new FieldOptionBean(fieldOptionDto.getText(), fieldOptionDto.getValue());
            }
        });
        return new FieldDetailsBean(parsedField.getId(), parsedField.getName(), parsedField.getTypeKey(), parsedField.getDescription(), options, null, false);
    }

    private FieldDetailsBean getSystemField(Project project, IssueType issueType, ApplicationUser user, OrderableField orderableField,
                                            FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        // fetch options for the field if there are any
        List<FieldOptionBean> options = systemFieldOptionService.getOptions(orderableField.getNameKey(), project, user);

        // Create a system field bean
        String defaultValue = systemFieldOptionService.getDefaultValue(orderableField, getIssueObject(project, issueType));
        String description = fieldScreenRenderLayoutItem.getFieldLayoutItem().getFieldDescription();

        return new FieldDetailsBean(orderableField.getId(), orderableField.getName(), orderableField.getNameKey(), description, options,
                defaultValue, true);
    }

    /*
     * Returns a CustomFieldBean with information for the requested customField
     */
    private FieldDetailsBean getCustomFieldBean(String customFieldId, Project project, IssueType issueType, ErrorCollection errorCollection,
                                                FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        CustomField customField = jiraCustomFieldManager.getCustomFieldObject(customFieldId);
        if (customField == null) {
            errorCollection.addError(getText("fields.resource.customfield.not.found"), customFieldId);
            return null;
        }

        List<FieldOptionBean> options = customFieldOptionService.getOptions(customField, project, issueType);

        // null value below is the URI - consider removing
        FieldDetailsBean bean = new FieldDetailsBean(customField, options, false);
        customFieldOptionService.attachCustomFieldDefaultValue(bean, customField, getIssueObject(project, issueType));

        return bean;
    }

    /*
     * The issue that gets returned here is simply a wrapper for the project and projectId. JIRA does it too.
     */
    public MutableIssue getIssueObject(Project p, IssueType t) {
        MutableIssue issueObject = jiraIssueFactory.getIssue();
        issueObject.setProjectId(p.getId());
        issueObject.setIssueTypeId(t.getId());

        return issueObject;
    }
}
