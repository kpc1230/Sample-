AJS.$(function() {
	if(!AJS.FormPopup) AJS.FormPopup = JIRA.FormDialog;
	
	var refreshPage = function(){
	    AJS.reloadViaWindowLocation(window.location.href);
	};

    var standardAjaxOptions = {
        data: {
            inline: true,
            decorator: "dialog"
        }
    };

    /**
     * This code is taken from JIRA 5.0.  We dont need to do this in a post 5.0 world but we do until we get
     * there so for now its copied and replicated on the server
     *
     * @param xhr
     */
    var _detectMsgInstructions = function (xhr, text) {
        var instructions = {
                preMsg: xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Html-Pre"),
                postMsg: xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Html-Post")
            };

        if (instructions.preMsg) {
            instructions.msg = instructions.preMsg + text + instructions.postMsg; 
            var msgType = xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Type");
            instructions.type = msgType ? msgType.toUpperCase() :  msgType;
            instructions.closeable = (xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Closeable") === "true");
            instructions.target = xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Target");
            return instructions;
        }
    };

    var _removeMsgFromSessionStorage = function() {

        if (JIRA.SessionStorage) {
            var SESSION_MSG_KEY = "jira.messages.reloadMessageMsg",
                SESSION_MSG_TYPE_KEY = "jira.messages.reloadMessageType",
                SESSION_MSG_CLOSEABLE_KEY = "jira.messages.reloadMessageCloseable",
                SESSION_MSG_TARGET_KEY = "jira.messages.reloadMessageTarget";

            JIRA.SessionStorage.removeItem(SESSION_MSG_KEY);
                        JIRA.SessionStorage.removeItem(SESSION_MSG_TYPE_KEY);
                        JIRA.SessionStorage.removeItem(SESSION_MSG_CLOSEABLE_KEY);
                        JIRA.SessionStorage.removeItem(SESSION_MSG_TARGET_KEY);
        }

    };


    var onSuccessfulSubmitHandler = function(data, xhr, textStatus, smartAjaxResult) {
        if (this.options.bonfireStayOnPage) {
            var thisSessionName = AJS.escapeHtml(jQuery(this.$form).find('#ex-session-name').val());
            var msgInstructions = _detectMsgInstructions(xhr, thisSessionName);

            if (msgInstructions) {
                if (JIRA.Messages === undefined) {
                    Bonfire.Messages.showMsg(msgInstructions);
                } else {
                    JIRA.Messages.showMsg(msgInstructions.msg, {
                                    type: JIRA.Messages.Types[msgInstructions.type],
                                    closeable: msgInstructions.closeable,
                                    target: msgInstructions.target
                                });
                    // we need to do this so that the detected message doesn't stay in
                    // session storage and hence get re-displayed in JIRA 5.x
                    _removeMsgFromSessionStorage();
                }
            }
        }
    };

    var onDialogFinishedHandler = function(data, xhr, textStatus, smartAjaxResult) {
        // if we don't provide this then JIRA will call its default which will refresh the page
        if (this.options.bonfireStayOnPage) {
            return;
        }
        refreshPage();

    };

    new AJS.FormPopup({
        trigger: "#create-test-session.no-refresh",
        id: "capture-create-test-session",
        autoClose: true,
        bonfireStayOnPage : true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
    });

    new AJS.FormPopup({
        trigger: ".bf-create-session.no-refresh",
        id: "capture-create-test-session",
        autoClose: true,
        bonfireStayOnPage : true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
    });

    new AJS.FormPopup({
        trigger: "#create-test-session:not(.no-refresh)",
        id: "capture-create-test-session",
        autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
    });

    new AJS.FormPopup({
		trigger: ".issueaction-create-test-session",
        id: "capture-create-test-session",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
		ajaxOptions: standardAjaxOptions
	});

	new AJS.FormPopup({
		trigger: "#clone-test-session",
		autoClose: true,
		bonfireStayOnPage : true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

    new AJS.FormPopup({
        trigger: "#assign-test-session",
        autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
    });

	new AJS.FormPopup({
		trigger: "#edit-test-session",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

	new AJS.FormPopup({
		trigger: "#delete-test-session",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

	new AJS.FormPopup({
		trigger: "#pause-test-session",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});
	
	new AJS.FormPopup({
		trigger: "#unshare-test-session-confirm",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});
	
	new AJS.FormPopup({
		trigger: "#add-raised-to-session",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

	new AJS.FormPopup({
		trigger: ".bf-add-raised-issues-link",
		autoClose: true,
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

	new AJS.FormPopup({
		trigger: 'a[title="Create test session"][class="aui-list-item-link"]',
        onSuccessfulSubmit : onSuccessfulSubmitHandler,
        onDialogFinished : onDialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
	});

    // auto complete
    jQuery(document).bind("dialogContentReady", function (e, dialogInstance) {
        var $assigneeInput = bfjQueryoverride("#ex-assignee");
        $assigneeInput.autocomplete({
            source: function(request, response) {
                jQuery.getJSON(contextPath + "/rest/bonfire/latest/userSearch", request, function(data) {
                    response(data.searchResult);
                });
            },
            minLength: 1
        });

        AJS.$(document.body).find('.bonfire-issuepicker').each(function () {
            var $thisSelect = AJS.$(this); 
            /* JIRA 5 compat */
            var width = 274;
            new JIRA.IssuePicker({
                element: $thisSelect,
                width: width,
                uppercaseUserEnteredOnSelect: true
            });
            
            AJS.$('#' + $thisSelect.attr("id") + '-textarea').attr("tabindex", $thisSelect.attr("tabindex"));
        });
        BF.bindCompleteSessionCommon(e, dialogInstance);
        jQuery('#bonfire-complete-button').bind('click', function(e) {
            var $thisButton = jQuery(this);
            BF.bindCompleteSubmit(e, $thisButton, refreshPage);
        });
    });
    
    // This is cheating. This .js file happens to be included on the view issue page so this method was placed here
    jQuery('.bf-twixy').live('click', function(e){
        var $twixy = jQuery(this);
        $twixy.toggleClass('bf-twix-open');
    });
    
    /**********************************************
    * Events below here are for the issue web-panel. 
    * They are prefixed with 'gf' because they also show up on the gh details view
    ***********************************************/
    
    var doSmartAjax = function(url, reqType) {
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: reqType,
            dataType: "json",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                refreshPage();
            }
        });
    }

    jQuery('.bf-delete-raised-in').live('click', function(e) {
        e.preventDefault();
        if(confirm('Are you sure you want to remove this issue from this test session\'s list of raised issues?')){
            var url = this.href;
            doSmartAjax(url, 'DELETE');
        }
    });

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
	    
    var initialiseWebPanel = function() {	
        // Make each view-session button an inline dialog
        jQuery.each(jQuery('.bf-view-session-button'), function(){
            var dialogId = 'bf-dialog';
            var currentTrigger = jQuery(this);
            BF.quickSessionCreate(currentTrigger, dialogId);            
        });

        try{
            AJS.Dropdown.create({
                trigger: jQuery(".bf-more-button .bf-more-trigger"),
                content: jQuery(".bf-more-button .aui-list"),
                alignment: AJS.RIGHT
            });
        } catch (err) {
            // Ignore. It means we are on IE8 and an out-of-context error is thrown.
        }
    }
    
    /**
    * These trigger whenever JIRA updates itself.
    */
    // onReady
    initialiseWebPanel();
    // every other time
    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function(e, context, reason) {
        initialiseWebPanel();
    });
});