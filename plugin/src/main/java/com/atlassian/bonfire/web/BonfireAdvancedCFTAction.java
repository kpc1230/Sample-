package com.atlassian.bonfire.web;

import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.AdvancedCFLoaderService;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfireJiraHelperService;
import com.atlassian.bonfire.service.controller.AdvancedCFController;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.json.JSONObject;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public class BonfireAdvancedCFTAction extends ExcaliburWebActionSupport {
    @JIRAResource
    private WebResourceManager webResourceManager;
    @JIRAResource
    private BuildUtilsInfo buildUtilsInfo;
    @JIRAResource
    private JiraAuthenticationContext authenticationContext;
    @JIRAResource
    private ApplicationProperties applicationProperties;

    @Resource(name = AdvancedCFLoaderService.SERVICE)
    private AdvancedCFLoaderService advancedCFService;

    @Resource(name = BonfireJiraHelperService.SERVICE)
    private BonfireJiraHelperService jiraHelperService;

    @Resource(name = AdvancedCFController.SERVICE)
    private AdvancedCFController advancedCFController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    private String pid;
    private String itid;
    private String rid;

    private List<String> renderedFields;
    private Map<String, String> metaFields;
    private boolean hasSaved;

    private Project project;
    private IssueType issueType;

    private ErrorCollection errorCollection = new ErrorCollection();

    // TODO refactor me to use the controller
    public String doView() {
        insertResources();

        Long projectId = safeConvert(pid);
        Long issueTypeId = safeConvert(itid);
        String errorRedirect = getErrorRedirect(false, true, getRedirectUrl(projectId, issueTypeId, rid), "inline");
        if (errorRedirect != null) {
            return errorRedirect;
        }
        ApplicationUser user = getLoggedInApplicationUser();
        project = jiraHelperService.getAndValidateProject(user, projectId, errorCollection);
        issueType = jiraHelperService.getAndValidateIssueType(user, issueTypeId, errorCollection);
        validateRID(rid, errorCollection);

        if (errorCollection.hasErrors()) {
            return ERROR;
        }

        // See if there are existing values
        JSONObject existing = advancedCFController.getAdvancedCFAsJSON(user, projectId, issueTypeId, rid);
        hasSaved = existing != null;
        renderedFields = advancedCFService.getAdvancedCustomFields(user, project, issueType);

        return SUCCESS;
    }

    private void insertResources() {
        final ApplicationUser loggedInUser = getLoggedInUser();
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-advanced-cft");
        webResourceManager.requireResource("jira.webresources:jira-fields");
        webResourceManager.requireResource("jira.webresources:calendar");
        webResourceManager.requireResourcesForContext("jira.issue.create");
        // TODO remove this map once we drop 4.4 and move to 'blank'
        metaFields = Maps.newHashMap();
        metaFields.put("context-path", request.getContextPath());
        metaFields.put("version-number", buildUtilsInfo.getVersion());
        metaFields.put("build-number", buildUtilsInfo.getCurrentBuildNumber());
        metaFields.put("remote-user", loggedInUser != null ? loggedInUser.getName() : "");
        metaFields.put("remote-user-fullname", loggedInUser != null ? loggedInUser.getDisplayName() : "");
        metaFields.put("user-locale", authenticationContext.getLocale().toString());
        metaFields.put("app-title", applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE));
    }

    private void validateRID(String rid, ErrorCollection errorCollection) {
        if (StringUtils.isBlank(rid)) {
            errorCollection.addError(i18n.getText("advanced.cft.invalid.rid"));
        }
    }

    private Long safeConvert(String s) {
        try {
            Long converted = Long.valueOf(s);
            return converted;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getRedirectUrl(Long pid, Long itid, String rid) {
        return BonfireConstants.ADVANCED_CFT_PAGE + "pid=" + pid + "&itid=" + itid + "&rid=" + rid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setItid(String itid) {
        this.itid = itid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getPid() {
        return pid;
    }

    public String getItid() {
        return itid;
    }

    public String getRid() {
        return rid;
    }

    public List<String> getRenderedFields() {
        return renderedFields;
    }

    public Map<String, String> getMetaFields() {
        return metaFields;
    }

    public ErrorCollection getErrorCollection() {
        return errorCollection;
    }

    public boolean isHasSaved() {
        return hasSaved;
    }

    public String getProjectKey() {
        return project != null ? project.getKey() : "";
    }

    public String getIssueTypeName() {
        return issueType != null ? issueType.getName() : "";
    }

    public String getIssueTypeIconUrl() {
        return issueType != null ? getUtil().getFullIconUrl(issueType) : "";
    }
}
