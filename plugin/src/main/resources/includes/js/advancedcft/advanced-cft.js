jQuery(function ($) {
    var enableSubmitButton, disableSubmitButton, advancedSubmitHandler, voidSubmit;
    // This is used to fix the ?? bug. See BON-1065
    var bonfireJSONStringify = function(data) {
        var stringified = JSON.stringify(data);
        if ( stringified && typeof(stringified) === 'string' ) {
            if ( /\?\?/.test(stringified) ) { 
                var questionMark = "\\" + "u003F"; 
                stringified = stringified.replace(/\?\?/g, questionMark + questionMark); 
            }
        }
        return stringified;
    }
    
    enableSubmitButton = function() {
        jQuery('#advanced-save-button').removeClass('disabled');
        jQuery('.advanced-cf-form').bind('submit', advancedSubmitHandler);
        jQuery('.advanced-cf-form').unbind('submit', voidSubmit);
    }

    disableSubmitButton = function() {
        jQuery('#advanced-save-button').addClass('disabled');
        jQuery('.advanced-cf-form').unbind('submit', advancedSubmitHandler);
        jQuery('.advanced-cf-form').bind('submit', voidSubmit);
    }
    
    voidSubmit = function () {
        return false;
    }
    
    advancedSubmitHandler = function(e) {
        var $advancedCFForm = jQuery(this);
        var $elements = e.target.elements;
        var url = contextPath + '/rest/bonfire/1.0/temp/advancedcft';
        var data = {}; 
        for (var i = 0, ii = $elements.length; i < ii; i++) {
            var elementName = $elements[i].name; 
            // Custom fields need to be an array
            if ( elementName.match(/^custom/) ) {
                if ( !data[elementName] ) {
                    data[elementName] = [];
                }
                data[elementName].push($elements[i].value);
            } else {
                // Everything else just put in there as is
                data[elementName] = $elements[i].value;
            }
        }
        jQuery('#advanced-cf-errors').empty();
        disableSubmitButton();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: bonfireJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) 
                {
                    jQuery('.saved-indicator').removeClass('warning').addClass('success');
                }
                else 
                {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#advanced-cf-errors').append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                    enableSubmitButton();
                }
            }
        });
        return false;
    }
    
    jQuery('#advanced-field-form').bind('change', function (e) {
        enableSubmitButton();
    });
    jQuery('.advanced-cf-form').bind('submit', voidSubmit);
});