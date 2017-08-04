AJS.$(function() {
    var sendSettings = function() {
        var submitButton = jQuery(this);
        var $container = jQuery('#bonfire-settings');
        submitButton.attr("aria-disabled", true);
        submitButton.unbind('click', sendSettings);
        var successMessage = $container.find('.bf-settings-success');
        successMessage.addClass('hidden');
        var errorMessage = $container.find('.bf-settings-errors');
        errorMessage.addClass('hidden');
        errorMessage.find("p").remove();
        var $feedbackCheck = $container.find('.bf-feedback-setting');
        var feedback = BF.utils.bonGetProp($feedbackCheck, 'checked');
        var $businessProjectsEnabledCheck = $container.find('.capture-business-projects-setting');
        var businessProjectsEnabled = !!BF.utils.bonGetProp($businessProjectsEnabledCheck, 'checked'); // hidden in JIRA before  7.0.0
        var $servicedeskProjectsEnabledCheck = $container.find('.capture-servicedesk-projects-setting');
        var servicedeskProjectsEnabled = !!BF.utils.bonGetProp($servicedeskProjectsEnabledCheck, 'checked'); // hidden in JIRA before 7.0.0
        var url = contextPath + '/rest/bonfire/1.0/settings/all';
        var data = {
            analytics: true, // we're re-using JIRA analytics settings
            feedback: feedback,
            serviceDeskProjectsEnabled: servicedeskProjectsEnabled,
            businessProjectsEnabled: businessProjectsEnabled
        };
        JIRA.SmartAjax.makeRequest({
            url: url,
            type: "POST",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                submitButton.attr("aria-disabled", false);
                submitButton.bind('click', sendSettings);
                if (smartAjaxResult.successful) 
                {
                    successMessage.removeClass('hidden');
                    if (jQuery.browser.msie && jQuery.browser.version < "9") {
                        submitButton.blur(); //reflow issue in ie8
                    }
                }
                else
                {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        errorMessage.append('<p>' + errorArray[error].errorMessage + '</p>');
                    }
                    errorMessage.removeClass('hidden');
                }
            }
        });
    };
    
    jQuery('.bf-settings-submit').bind('click', sendSettings);
    
    //Fix for tabs bug
    jQuery('#bonfire-license-link_tab').bind('click', function() {
        AJS.reloadViaWindowLocation(this.href);
    });
});
