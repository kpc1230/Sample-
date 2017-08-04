AJS.$(function(){
    var standardAjaxOptions = {
        data: {
            inline: true,
            decorator: "dialog"
        }
    };
    
    var refreshRapidBoard = function() {
        JIRA.trigger('GH.RapidBoard.causeBoardReload');
    };

    var onDialogFinishedHandler = function(data, xhr, textStatus, smartAjaxResult) {
        refreshRapidBoard();
        return;
    };
    
    var doSmartAjax = function(url, reqType) {
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: reqType,
            dataType: "json",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                // TODO error handling
                refreshRapidBoard();
            }
        });
    }
    
    jQuery('.gf-post-trigger').live('click', function(e) {
        e.preventDefault();
        var url = this.href;
        doSmartAjax(url, 'POST');
    });

    jQuery('.gf-delete-trigger').live('click', function(e) {
        e.preventDefault();
        var url = this.href;
        doSmartAjax(url, 'DELETE');
    });

    new AJS.FormPopup({
		trigger: ".gf-dialog-link-trigger",
		autoClose: true,
		onDialogFinished: onDialogFinishedHandler,
		ajaxOptions: standardAjaxOptions
	});

    new AJS.FormPopup({
		trigger: ".gf-create-session-button",
		autoClose: true,
		onDialogFinished: onDialogFinishedHandler,
		ajaxOptions: standardAjaxOptions
	});

	var CompleteDialog = AJS.FormPopup.extend({
        _submitForm: function (e) {
            e.preventDefault();
        }
	});
	
    new CompleteDialog({
		trigger: "#gf-complete-dialog",
        id: "capture-complete-test-session",
		autoClose: true,
		onSuccessfulSubmit: onDialogFinishedHandler,
		onUnSuccessfulSubmit: onDialogFinishedHandler,
		onDialogFinished: onDialogFinishedHandler,
		ajaxOptions: standardAjaxOptions,
		width: "100%"
	});
	
	// Issue action link in the cog-dropdown and dot-dialog
    new AJS.FormPopup({
		trigger: ".issueaction-create-test-session",
        id: "capture-create-test-session",
		autoClose: true,
		onDialogFinished: onDialogFinishedHandler,
		ajaxOptions: standardAjaxOptions
	});
    
    /**
    * GH.DetailView.updated event
    **/
    
    JIRA.bind("GH.DetailView.updated", function() {
        AJS.Dropdown.create({
            trigger: jQuery(".gf-more-button .gf-more-trigger"),
            content: jQuery(".gf-more-button .aui-list"),
            alignment: AJS.RIGHT
        });
        
        // Make each view-session button an inline dialog
        jQuery.each(jQuery('.gf-view-session-button'), function(){
            var dialogId = 'bf-dialog';
            var currentTrigger = jQuery(this);
            BF.quickSessionCreate(currentTrigger, dialogId);
        });
    });

	/**
	* dialogContentReady event
	**/
	
	JIRA.bind("dialogContentReady", function (e, dialogInstance) {
        // Adjusting the height of the details dialog
        var maxHeight = Math.max(0, jQuery(window).height() - 200);
        jQuery('.bfd-full-body').css('max-height', maxHeight);
        
        // User picker in create dialog
        var $assigneeInput = bfjQueryoverride("#ex-assignee");
        $assigneeInput.autocomplete({
            source: function(request, response) {
                jQuery.getJSON(contextPath + "/rest/bonfire/latest/userSearch", request, function(data) {
                    response(data.searchResult);
                });
            },
            minLength: 1
        });
        BF.bindCompleteSessionCommon(e, dialogInstance);
        jQuery('#bonfire-complete-button').bind('click', function(e) {
            var $thisButton = jQuery(this);
            BF.bindCompleteSubmit(e, $thisButton, refreshRapidBoard);
        });
	});
});