package com.atlassian.bonfire.pageobjects.admin;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.upm.pageobjects.*;
import org.openqa.selenium.By;

public class CaptureLicensePage extends CaptureAbstractJiraPageObject {
    private static final String URI = "/plugins/servlet/upm#manage";

    @ElementBy(id = "upm-manage-container", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement upmPluginsPage_2_17;

    @ElementBy(id = "upm-manage-user-installed-plugins", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement upmPluginsPage_2_19;

    public String getUrl() {
        return URI;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimedCondition isAt() {
        Conditions.CombinableCondition upm2_17_loaded = Conditions.and(
                upmPluginsPage_2_17.timed().isPresent(),
                Conditions.not(upmPluginsPage_2_17.timed().hasClass("loading")));

        Conditions.CombinableCondition upm2_19_loaded = Conditions.and(
                upmPluginsPage_2_19.timed().isPresent(),
                upmPluginsPage_2_19.timed().hasClass("loaded"));

        return Conditions.or(upm2_17_loaded, upm2_19_loaded);

    }

    public CaptureLicensePage enterLicense(String license) {
        PluginManager upm = pageBinder.navigateToAndBind(PluginManager.class);
        InstalledPlugin bonfirePlugin = upm.openManageExistingPage().getPlugin("com.atlassian.bonfire.plugin");
        InstalledPluginDetails bonfirePluginDetails = bonfirePlugin.openPluginDetails();

        final Message message = getMessage(license, bonfirePluginDetails);
        message.waitUntilMessageAppears();

        return this;
    }

    private Message getMessage(String license, InstalledPluginDetails bonfirePluginDetails) {
        // Breaking UPM changes - different return types in different versions.
        // Making it compile with different JIRA versions by using Reflection APIs
        Object result = getMessageRaw(license, bonfirePluginDetails);
        final Message message;
        if (result instanceof Message) {
            message = (Message) result;
        } else if (result instanceof InstalledPluginDetails) {
            TimedCondition query = pageElementFinder.find(By.id("upm-plugin-status-dialog")).timed().isPresent();
            final Boolean pluginPopupIsShown = query.byDefaultTimeout();
            if (pluginPopupIsShown) {
                // UPM 2.17 and earlier - popup is shown
                final PluginStatusDialog pluginStatusDialog = pageBinder.bind(PluginStatusDialog.class);
                pluginStatusDialog.close();
                message = ReflectionKit.method(bonfirePluginDetails, "waitForInlineStatusMessage").call();
            } else {
                // For the cases when no popup is shown by UPM
                bonfirePluginDetails = (InstalledPluginDetails) result;
                message = ReflectionKit.method(bonfirePluginDetails, "waitForInlineStatusMessage").call();
            }
        } else {
            throw new IllegalStateException(result + " type is not supported");
        }
        return message;
    }

    private Object getMessageRaw(String license, InstalledPluginDetails bonfirePluginDetails) {
        return bonfirePluginDetails.editLicenseKey(license);
    }

    public TimedQuery<Boolean> hasError() {
        return elementFinder.find(By.cssSelector(".aui-message.error")).timed().isVisible();
    }

}
