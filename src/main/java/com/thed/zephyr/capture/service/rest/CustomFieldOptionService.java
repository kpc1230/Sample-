package com.thed.zephyr.capture.service.rest;

import com.thed.zephyr.capture.rest.model.FieldDetailsBean;
import com.thed.zephyr.capture.rest.model.FieldOptionBean;
import com.thed.zephyr.capture.rest.model.VersionOptionBean;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.usercompatibility.UserCompatibilityHelper;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

import static com.atlassian.bonfire.web.util.JiraFieldConstants.*;

@Service(CustomFieldOptionService.SERVICE)
public class CustomFieldOptionService {
    public static final String SERVICE = "bonfire-customfieldoptionservice";

    @Resource
    private OptionsManager jiraOptionsManager;

    @Resource
    private GroupManager jiraGroupManager;

    @Resource
    private VersionManager jiraVersionManager;

    public void attachCustomFieldDefaultValue(FieldDetailsBean bean, CustomField customField, Issue issue) {
        Object defaultValueRaw = customField.getDefaultValue(issue);
        //If there is no default value
        if (defaultValueRaw == null) {
            return;
        }
        String issueTypeKey = customField.getCustomFieldType().getKey();
        String defaultValue = null;
        List<String> defaultValueList = null;
        if (DATE_PICKER_KEY.equals(issueTypeKey)) {
            defaultValue = DateFormatUtils.format((Timestamp) defaultValueRaw, "dd/MMM/yy");
        } else if (DATE_TIME_KEY.equals(issueTypeKey)) {
            defaultValue = DateFormatUtils.format((Timestamp) defaultValueRaw, "dd/MMM/yy hh:mm a");
        } else if (FREE_TEXT_AREA_KEY.equals(issueTypeKey) || TEXT_FIELD_KEY.equals(issueTypeKey) || URL_FIELD_KEY.equals(issueTypeKey)) {
            defaultValue = (String) defaultValueRaw;
        } else if (SELECT_KEY.equals(issueTypeKey) || RADIO_BUTTON_KEY.equals(issueTypeKey)) {
            defaultValue = getOptionForSelect(defaultValueRaw);
        } else if (PROJECT_PICKER_KEY.equals(issueTypeKey)) {
            final Project project = (Project) defaultValueRaw;
            defaultValue = String.valueOf(project.getId());
        } else if (NUMBER_KEY.equals(issueTypeKey)) {
            defaultValue = String.valueOf(defaultValueRaw);
        } else if (LABEL_KEY.equals(issueTypeKey)) {
            defaultValue = getLabelString(defaultValueRaw);
        } else if (GROUP_PICKER_KEY.equals(issueTypeKey) || VERSION_KEY.equals(issueTypeKey)) {
            defaultValue = getSingleDefaultvalueFromArrayList(defaultValueRaw, issueTypeKey);
        } else if (MULTI_CHECKBOX_KEY.equals(issueTypeKey) || MULTI_SELECT_KEY.equals(issueTypeKey) || MULTI_VERSION_KEY.equals(issueTypeKey)
                || MULTI_GROUP_PICKER_KEY.equals(issueTypeKey)) {
            defaultValueList = getDefaultValuesFromArrayList(defaultValueRaw, issueTypeKey);
        } else if (MULTI_USER_PICKER_KEY.equals(issueTypeKey)) {
            defaultValue = getDefaultValuesForMultiUsers(defaultValueRaw);
        } else if (CASCADING_SELECT_KEY.equals(issueTypeKey)) {
            defaultValueList = getDefaultValuesForCascadingSelect(defaultValueRaw);
        } else if (USER_PICKER_KEY.equals(issueTypeKey)) {
            defaultValue = UserCompatibilityHelper.convertUserObject(defaultValueRaw).getUser().getName();
        } else {
            return;
        }

        // Attach the default values
        if (defaultValue != null && defaultValueList == null) {
            bean.setDefaultValueString(defaultValue);
        } else if (defaultValue == null && defaultValueList != null) {
            bean.setDefaultValue(defaultValueList);
        }
    }

    public List<FieldOptionBean> getOptions(CustomField customField, Project project, IssueType issueType) {
        List<FieldOptionBean> customFieldOptions = new ArrayList<FieldOptionBean>();
        if (GROUP_PICKER_KEY.equals(customField.getCustomFieldType().getKey())
                || MULTI_GROUP_PICKER_KEY.equals(customField.getCustomFieldType().getKey())) {

            addCustomFieldOptionBeansForGP(customFieldOptions);
        } else if (VERSION_KEY.equals(customField.getCustomFieldType().getKey()) || MULTI_VERSION_KEY.equals(customField.getCustomFieldType().getKey())) {
            addVersionOptions(customFieldOptions, project);
        } else {
            // Ideally, I would like the getOptions method to be project and issueType independent...
            FieldConfig config = customField.getRelevantConfig(new IssueContextImpl(project.getId(), issueType.getId()));
            addCustomFieldOptionBeans(config, customFieldOptions);
        }

        return customFieldOptions;
    }

    /*
     * Returns a list of options for the customField
     */
    @SuppressWarnings("unchecked")
    private void addCustomFieldOptionBeans(FieldConfig config, List<FieldOptionBean> beans) {
        Collection<Option> options = (Collection<Option>) jiraOptionsManager.getOptions(config);
        if (options == null) {
            return;
        }
        for (Option option : options) {
            if (!isOptionDisabled(option)) {
                // Check for child options
                Collection<Option> childOptions = jiraOptionsManager.findByParentId(option.getOptionId());
                if (childOptions != null && !childOptions.isEmpty()) {
                    // If there are children then fill them in
                    List<FieldOptionBean> children = new ArrayList<FieldOptionBean>();
                    for (Option child : childOptions) {
                        if (!isOptionDisabled(child)) {
                            children.add(new FieldOptionBean(child.getValue(), child.getOptionId().toString()));
                        }
                    }
                    // Then create an option with children
                    // Cascading selects use the optionId in xmlrpc
                    beans.add(new FieldOptionBean(option.getValue(), option.getOptionId().toString(), children));
                } else {
                    beans.add(new FieldOptionBean(option.getValue(), option.getOptionId().toString()));
                }
            }
        }
    }

    /*
     * Checks if the option is disabled or not. Default to false - default to not disabled.
     */
    private boolean isOptionDisabled(Option option) {
        return ReflectionKit.methodWithDefault(option, "getDisabled", false).<Boolean>call();
    }

    /*
     * Returns a list of options for the customfield if it is a group picker
     */
    private void addCustomFieldOptionBeansForGP(List<FieldOptionBean> customFieldOptions) {
        // TODO Use a PermissionsManager to get all the groups, so we don't see groups we're not meant to see.
        for (Group g : jiraGroupManager.getAllGroups()) {
            customFieldOptions.add(new FieldOptionBean(g.getName(), g.getName()));
        }
    }

    private void addVersionOptions(List<FieldOptionBean> customFieldOptions, Project project) {
        for (Version version : jiraVersionManager.getVersionsUnarchived(project.getId())) {
            customFieldOptions.add(new VersionOptionBean(version.getName(), version.getId().toString(), version.isReleased()));
        }
    }

    private String getDefaultValuesForMultiUsers(Object defaultValueRaw) {
        StringBuilder sb = new StringBuilder();
        ArrayList list = (ArrayList) defaultValueRaw;
        for (Object o : list) {
            if (o != null) {
                String username = UserCompatibilityHelper.convertUserObject(o).getUser().getName();
                sb.append(username).append(", ");
            }
        }
        return sb.toString();
    }

    private String getSingleDefaultvalueFromArrayList(Object defaultValueRaw, String typeKey) {
        ArrayList list = (ArrayList) defaultValueRaw;
        if (!list.isEmpty()) {
            Object o = list.iterator().next();
            if (o != null) {
                if (GROUP_PICKER_KEY.equals(typeKey)) {
                    return ((Group) o).getName();
                } else if (VERSION_KEY.equals(typeKey)) {
                    return ((Version) o).getId().toString();
                }
            }
        }

        return null;
    }

    private String getLabelString(Object defaultValueRaw) {
        StringBuilder sb = new StringBuilder();
        HashSet labelSet = (HashSet) defaultValueRaw;
        for (Object o : labelSet) {
            if (o != null) {
                sb.append(((Label) o).getLabel()).append(" ");
            }
        }
        return sb.toString();
    }

    private ArrayList<String> getDefaultValuesFromArrayList(Object defaultValueRaw, String typeKey) {
        ArrayList<String> defaultValueList = new ArrayList<String>();
        ArrayList valueList = (ArrayList) defaultValueRaw;
        for (Object o : valueList) {
            if (o != null) {
                if (MULTI_CHECKBOX_KEY.equals(typeKey) || MULTI_SELECT_KEY.equals(typeKey)) {
                    addOptionForSelect(defaultValueList, o);
                } else if (MULTI_VERSION_KEY.equals(typeKey)) {
                    defaultValueList.add(((Version) o).getId().toString());
                } else if (MULTI_GROUP_PICKER_KEY.equals(typeKey)) {
                    defaultValueList.add(((Group) o).getName());
                }
            }
        }
        return defaultValueList;
    }

    private void addOptionForSelect(List<String> defaultValueList, Object o) {
        defaultValueList.add(((Option) o).getOptionId().toString());
    }

    private String getOptionForSelect(Object o) {
        return ((Option) o).getOptionId().toString();
    }

    private ArrayList<String> getDefaultValuesForCascadingSelect(Object defaultValueRaw) {
        ArrayList<String> defaultValueList = new ArrayList<String>();
        //Post 5.0 uses HashMap. Pre 5.0 uses CustomFieldParamsImpl
        if (defaultValueRaw instanceof HashMap) {
            HashMap cpi = ((HashMap) defaultValueRaw);
            // The key for the first value is against the null key
            Option nullValue = (Option) cpi.get(null);
            addOptionForCascadingSelect(defaultValueList, nullValue);
            // The key for the second value is against the 1 key
            Option oneValue = (Option) cpi.get("1");
            addOptionForCascadingSelect(defaultValueList, oneValue);
        } else if (defaultValueRaw instanceof CustomFieldParamsImpl) {
            CustomFieldParamsImpl cpi = ((CustomFieldParamsImpl) defaultValueRaw);
            // The key for the first value is against the null key
            Collection<String> nullCollection = cpi.getValuesForNullKey();
            addOptionForCascadingSelect(defaultValueList, nullCollection);
            // The key for the second value is against the 1 key
            Collection<String> oneCollection = cpi.getValuesForKey("1");
            addOptionForCascadingSelect(defaultValueList, oneCollection);
        }
        return defaultValueList;
    }

    private void addOptionForCascadingSelect(List<String> defaultValueList, Collection<String> options) {
        if (options != null && !options.isEmpty()) {
            defaultValueList.addAll(options);
        }
    }

    private void addOptionForCascadingSelect(List<String> defaultValueList, Option option) {
        if (option != null) {
            defaultValueList.add(option.getOptionId().toString());
        }
    }
}
