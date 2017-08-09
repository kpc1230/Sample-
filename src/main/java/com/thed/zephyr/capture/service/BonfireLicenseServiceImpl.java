package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.upm.api.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Service for manipulating Bonfire Licenses.
 */
@Service(BonfireLicenseService.SERVICE)
public class BonfireLicenseServiceImpl extends BonfireServiceSupport implements BonfireLicenseService, LifecycleAware {
    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource
    private PluginLicenseManager pluginLicenseManager;

    private void errorCaseHelper(Status status, ErrorCollection errorCollection) {
        switch (status) {
            case unactivated:
                errorCollection.addError(i18n.getText("license.error.not.activated"), "data");
                break;
            case invalid:
                errorCollection.addError(i18n.getText("license.error.not.valid"), "data");
                break;
            case userMismatch:
                errorCollection.addError(i18n.getText("license.error.user.mismatch"), "data");
                break;
            case expired:
                errorCollection.addError(i18n.getText("license.error.expired"), "data");
                break;
            case evalExpired:
                errorCollection.addError(i18n.getText("license.error.evaluation.expired"), "data");
                break;
            case maintenanceExpired:
                errorCollection.addError(i18n.getText("license.error.maintenance.expired"), "data");
                break;
            case requiresRestart:
                errorCollection.addError(i18n.getText("upgrade.tasks.not.run"), "data");
                break;
            case activated:
                // No errors in this case
                break;
        }
    }

    @Override
    public ErrorCollection getLicenseStatusErrors() {
        ErrorCollection errors = new ErrorCollection();
        errorCaseHelper(getLicenseStatus(), errors);
        return errors;
    }

    @Override
    public void validateLicense(ErrorCollection errorCollection, String license) {
        if (StringUtils.isEmpty(license)) {
            errorCollection.addError(i18n.getText("license.error.not.valid"), "data");
            return;
        }
        final PluginLicense pluginLicense = getLicense();
        Status status = BonfireLicenseStatusUtil.convert(pluginLicense);
        errorCaseHelper(status, errorCollection);
    }

    @Override
    public Status getLicenseStatus() {
        final PluginLicense pluginLicense = getLicense();
        return BonfireLicenseStatusUtil.convert(pluginLicense);
    }

    @Override
    public PluginLicense getLicense() {
        return pluginLicenseManager.getLicense().getOrElse((PluginLicense) null);
    }

    @Override
    public void setLicense(String license) {
        throw new UnsupportedOperationException("Please use UPM to set your Capture for JIRA license.");
    }

    @Override
    public void validateAndSetLicense(com.atlassian.jira.util.ErrorCollection jiraErrorCollection, String license) {
        ErrorCollection errorCollection = new ErrorCollection();
        validateLicense(errorCollection, license);
        if (errorCollection.hasErrors()) {
            // transfer into the JIRA view of the work
            transferToJIRAErrorCollection(jiraErrorCollection, errorCollection);
        } else {
            setLicense(license);
        }
    }

    @Override
    public void onStart() {
        // this is technically not needed but in order to be a public component in plusings land, one mujst have an interface
    }

    public void onStop() {
        // this is technically not needed but in order to be a public component in plusings land, one mujst have an interface
    }

    private void transferToJIRAErrorCollection(com.atlassian.jira.util.ErrorCollection jiraErrorCollection, ErrorCollection errorCollection) {
        for (ErrorCollection.ErrorItem errorItem : errorCollection.getErrors()) {
            if (errorItem.isFieldError()) {
                jiraErrorCollection.addError(errorItem.getField(), errorItem.getMessageKeyEscaped());
            } else {
                jiraErrorCollection.addErrorMessage(errorItem.getMessageKeyEscaped());
            }
        }
    }

    @Override
    public boolean isBonfireActivated() {
        Status bonfireStatus = getLicenseStatus();

        // OR in all other valid options
        return Status.activated.equals(bonfireStatus);
    }

    @Override
    protected void onPluginStart() {
    }

    @Override
    protected void onPluginStop() {
    }

    @Override
    protected void onClearCache() {
    }
}
