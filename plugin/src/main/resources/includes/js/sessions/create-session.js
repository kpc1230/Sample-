AJS.$(function() {
    /**
    * Spinner methods
    **/
    var spinnerSpec = { lines: 12, length: 3, width: 2, radius: 3, trail: 60, speed: 1.5 }; // Copied from Stash
    var spinnerTarget;
    var spinner;
    var startSpinner = function() {
        if (!spinner) {
            spinner = new Spinner(spinnerSpec).spin(spinnerTarget);
            jQuery('.bf-create-session-overlay').removeClass('hidden');
        }
    };
    var stopSpinner = function() {
        if (spinner) {
            spinner.stop();
            delete spinner;
            spinner = null;
            jQuery('.bf-create-session-overlay').addClass('hidden');
        }
    }

    /**
    * Internal Methods
    **/
    var initIssuePicker = function () {
        AJS.$(document.body).find('.bonfire-issuepicker').each(function () {
            var $thisSelect = AJS.$(this); 
            var width = 274;
            new JIRA.IssuePicker({
                element: $thisSelect,
                width: width,
                uppercaseUserEnteredOnSelect: true
            });
            // Setting the tabindex of the newly created element
            AJS.$('#' + $thisSelect.attr("id") + '-textarea').attr("tabindex", $thisSelect.attr("tabindex"));
            // Setting the min height to be the current height
            var mainForm = AJS.$('.bf-create-form-body');
            mainForm.css('min-height', mainForm.height())
        });
    }
        
    var updateProjectSpecificFields = function (selectedOption) {
        var projectKey = selectedOption.val();
        if (projectKey !== '') {
            var projectId = selectedOption.data('projectid');
            var url = contextPath + '/rest/bonfire/1.0/templates/defaults/' + projectKey;
            startSpinner();
            JIRA.SmartAjax.makeRequest({
                url:url,
                type: "GET",
                dataType: "json",
                contentType: "application/json",
                complete: function (xhr, textStatus, smartAjaxResult) {
                    if (smartAjaxResult.successful) {
                        var contents = jQuery('.bf-create-form-body.form-body');
                        var drawParams = {
                            defaultTemplates: smartAjaxResult.data.templates,
                            projectKey: projectKey,
                            projectId: projectId,
                            contextPath: contextPath
                        };
                        jQuery(BF.template.drawProjectSpecificFields(drawParams)).appendTo(contents);
                        initIssuePicker();
                        stopSpinner();
                    }
                }
            });
        }
    }
    
    /**
    * Dialog Ready Listener
    **/
    jQuery(document).bind("dialogContentReady", function (e, dialogInstance) {
        spinnerTarget = jQuery('.bf-spinner-container')[0];
        
        var $assigneeInput = bfjQueryoverride("#ex-assignee");
        $assigneeInput.autocomplete({
            source: function(request, response) {
                jQuery.getJSON(contextPath + "/rest/bonfire/latest/userSearch", request, function(data) {
                    response(data.searchResult);
                });
            },
            minLength: 1
        });
        
        jQuery('#related-project').change(function (e) {
            var selectedOption = jQuery(this.options[this.selectedIndex]);
            jQuery('.bf-dynamic-marker').remove();
            updateProjectSpecificFields(selectedOption);
        });
    });
});
