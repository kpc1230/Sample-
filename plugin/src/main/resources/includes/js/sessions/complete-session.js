if (typeof BF == 'undefined') { window.BF = {}; }

AJS.$(function() {
    /**
    * External Methods
    **/
    BF.bindCompleteSessionCommon = function(e, dialogInstance) {
        if (dialogInstance && dialogInstance.$form) {
            dialogInstance.$form.find('input[type=text]').keypress( function(e) {
                if (e.keyCode == 13 || e.keyCode == 10)
                {
                    jQuery('#bonfire-complete-button').click();
                }
            });
        }
    };
    
    BF.bindCompleteSubmit = function(e, $thisButton, successCallback) {
        e.preventDefault();
        var $completeForm = $thisButton.closest("form");
        var sessionId = $completeForm.find('[name=testSessionId]').val()
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/complete';
        var timeSpent = $completeForm.find('#bf-time-spent').val();
        // these only exists sometimes
        var $logTimeIssue = $completeForm.find('#bf-log-time-issue');
        var logTimeIssueId = "";
        if($logTimeIssue.length !== 0){
            logTimeIssueId = $logTimeIssue.val();
        }
        var relatedSelects = $completeForm.find('.bfcom-select');
        var issuesToLink = [];
        for (var i = 0, ii = relatedSelects.length; i < ii; i++) {
            var raisedId = jQuery(relatedSelects[i]).data('raised-id');
            var relatedId = relatedSelects[i].value;
            issuesToLink.push({
                raisedId: raisedId,
                relatedId: relatedId
            });
        }
        var data = {
            timeSpent: timeSpent,
            logTimeIssueId: logTimeIssueId,
            issueLinks: issuesToLink
        };
        jQuery('#error-complete-session').empty();
        $thisButton[0].disabled = true;
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    $completeForm.find('.cancel').click();
                    successCallback();
                }
                else {
                    $thisButton[0].disabled = false;
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#error-complete-session').append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        });
    };
});