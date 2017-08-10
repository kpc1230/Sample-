package com.thed.zephyr.capture.web;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.BonfireSessionCustomFieldService;
import com.thed.zephyr.capture.service.BonfireBuildCheckService;
import com.thed.zephyr.capture.service.BonfireLicenseService;
import com.thed.zephyr.capture.upgradetasks.UpgradeTaskKit;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.atlassian.bonfire.service.controller.BonfireCompleteSessionService.BONFIRE_TESTING;

/**
 * A page to display Bonfire information for support reasons.  It shows some information
 * to anonymous people and then more information to admins
 */
public class BonfireSupportAction extends ExcaliburWebActionSupport {
    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @Resource(name = UpgradeTaskKit.SERVICE)
    private UpgradeTaskKit upgradeTaskKit;

    @Resource(name = BonfireBuildCheckService.SERVICE)
    private BonfireBuildCheckService bonfireBuildCheckService;

    @JIRAResource
    private ApplicationProperties jiraApplicationProperties;

    @JIRAResource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    @JIRAResource
    private WebResourceManager webResourceManager;

    @JIRAResource
    private IssueLinkTypeManager issueLinkTypeManager;

    public String doSupport() {
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-shared");
        return SUCCESS;
    }

    public String getVersionsDirectory() {
        return buildPropertiesService.getVersionDirectory();
    }

    public boolean isAttachementsOn() {
        return jiraApplicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
    }

    public boolean isIssueLinkDefined() {
        jiraApplicationProperties.setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);

        Collection<IssueLinkType> links = issueLinkTypeManager.getIssueLinkTypesByName(BONFIRE_TESTING);
        return (links != null && !links.isEmpty());
    }

    public boolean isTimeTrackingOn() {
        return jiraApplicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    public String licenseStatus() {
        return bonfireLicenseService.getLicenseStatus().toString();
    }

    public String getJiraVersion() {
        return jiraBuildUtilsInfo.getVersion();
    }

    public String getJiraBuildInformation() {
        return jiraBuildUtilsInfo.getBuildInformation();
    }

    public List<UpgradeTaskKit.UpgradeTaskInfo> getUpgradeTaskInfo() {
        return upgradeTaskKit.getRunUpgradeTasks();
    }

    public String getHighestRunVersion() {
        String highestRunVersion = bonfireBuildCheckService.getHighestRunVersion();
        return StringUtils.isNotBlank(highestRunVersion) ? highestRunVersion : "Not Recorded";
    }

    public String getRaisedInFieldId() {
        CustomField field = bonfireSessionCustomFieldService.getRaisedInSessionCustomField();
        return field != null ? field.getId() : "";
    }

    public String getRelatedToFieldId() {
        CustomField field = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        return field != null ? field.getId() : "";
    }
}
