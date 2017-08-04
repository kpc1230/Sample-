(function ($) {
    var shareDialog, $contents;
    var allowDialogHide = false;

    function hideDialog(reset) {
        allowDialogHide = true;
        if (reset) {
            // We have to bind to the event triggered by the inline-dialog hide code, as the actual hide runs in a
            // setTimeout callback. This caused JRADEV-7962 when trying to empty the contents synchronously.
            $(document).one("hideLayer", function (e, type, dialog) {
                if (type == "inlineDialog" && dialog.popup == shareDialog) {
                    $(document).unbind('.share-dialog');
                    $contents.empty();
                }
            });
        }
        shareDialog.hide();
        return false;
    }

    function getUsernameValue() {
        return AJS.$(this).attr("data-username");
    }

    function getEmailValue() {
        return AJS.$(this).attr("data-email");
    }
    
    function localJSONStringify(request) {
        var requestStringified = JSON.stringify(request);
        if (/\?\?/.test(requestStringified)) { 
            var questionMark = "\\" + "u003F"; 
            requestStringified = requestStringified.replace(/\?\?/g, questionMark + questionMark);
        }
        return requestStringified; 
    }

    function submit() {
        // 0. Get the entered Users and Email addresses and abort if none found. Note that we can't just use
        // #sharenames.val() because we need to split out the different types. It might be nice if MultiSelect provided
        // a method for this...
        var $recipients = $contents.find('.recipients');
        var users = $recipients.find('li[data-username]').map(getUsernameValue).toArray();
        var emails = $recipients.find('li[data-email]').map(getEmailValue).toArray();
        if (!(users.length || emails.length)) {
            return false;
        }

        $("button,input,textarea", this).attr("disabled", "disabled");

        var icon = $contents.find(".button-panel .icon");
        icon.css("left", "10px").css("position", "absolute");
        icon.spin();

        var messages = $contents.find(".progress-messages");
        //i18n me
        messages.text("Sending");
        messages.css("padding-left", icon.innerWidth() + 5);

        var message = $contents.find("#note").val();
        var sessionId = $('#bonfire-session-id').val();
        var request = {
            usernames: users,
            emails: emails,
            message: message,
            sessionId: sessionId
        };
        var url = contextPath + '/rest/bonfire/1.0/invite';

        JIRA.SmartAjax.makeRequest({
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            url: url,
            data: localJSONStringify(request),
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    icon.spinStop();
                    icon.addClass('icon-tick');
                    //i18n me
                    messages.text('Sent');
                    setTimeout(function() {
                        hideDialog(true);
                    }, 1000);
                } else {
                    icon.spinStop();
                    icon.addClass('icon-cross');
                    //i18n me
                    messages.text('Error while sending');
                }
            }
        });

        return false;
    }

    function enableSubmit(enabled) {
        var submitBtn = $contents.find(".submit");
        if (!!submitBtn.prop) {
            submitBtn.prop("disabled", !enabled);
        } else {
            submitBtn.attr("disabled", !enabled);
        }
    }

    /**
     * Invoke a bunch of magical JS event delegation to make sure that we only trigger the execution of the functions
     * attached to the access keys defined within this dialog's form.
     *
     * @param shareDialogForm the share dialog's form.
     */
    function enableAccessKeys(shareDialogForm){
        AJS.$(shareDialogForm).handleAccessKeys({
            selective: false // only trigger the access keys defined in this form.
        });
    }

    function generatePopup(contents, trigger, doShowPopup) {
        allowDialogHide = false;
        $contents = contents;
        if ($contents.children().length) {
            // Dialog already opened once and not reset - just reuse it
            doShowPopup();
            return;
        }

        $contents.append($('#bonfire-invite-form').clone().show().click(
            function (e) {
                e.stopPropagation();
            })
        );
        if (AJS.$.browser.msie) {
            $contents.find("form").ieImitationPlaceholder();
        }
        enableSubmit(false);
        $contents.find('#sharenames').bind('change unselect', function () {
            var val = $(this).val();
            enableSubmit(val && val.length);
        });

        $contents.find(".close-dialog").click(function() {
            hideDialog(true);
        });

        $contents.find("form").submit(function() {
            submit();
            return false;
        });

        $(document).bind('keyup.share-dialog', function (e) {
            // Close on Escape key
            if (e.keyCode == 27) {
                return hideDialog(false);   // leave the dialog contents alone
            }
            return true;
        });

        $(document).bind("showLayer.share-dialog", function (e, type, dialog) {
            if (type == "inlineDialog" && dialog.popup == shareDialog) {
                $contents.find("#sharenames-textarea").focus();
            }
        });

        enableAccessKeys(AJS.$("form", $contents));

        doShowPopup();

        JIRA.trigger('bonfireInviteReady', [$contents]);
    }

    AJS.toInit(function ($) {
        var dialogOptions = {
            preHideCallback: function() {
                return allowDialogHide;
            },
            hideCallback: function () {
                $(".dashboard-actions .explanation").hide();
            },
            width: 273,
            offsetY: 17,
            offsetX: -100,
            hideDelay: 36e5,         // needed for debugging! Sit for an hour.
            useLiveEvents: true
        };

        shareDialog = AJS.InlineDialog($('#bonfire-invite-trigger'), "share-entity-popup", generatePopup, dialogOptions);

        // JRADEV-8136 - Clicking the share button again doesn't close the share dialog.
        $('#bonfire-invite-trigger').live("click", function() {
            if (shareDialog.find(".contents:visible").length) {
                shareDialog.find("a.close-dialog").click();
            }
        });

        //this is a hack, but it's necessary to stop click on the multi-select autocomplete from closing the
        //inline dialog. See JRADEV-8136
        AJS.$(document).bind("showLayer", function(e, type, hash) {
            if(type && type === "inlineDialog" && hash && hash.id && hash.id === "share-entity-popup") {
                $("body").unbind("click.share-entity-popup.inline-dialog-check");
            }
        });

        // JRA-27476 - share dialog doesn't stalk. So hide it, without reset, when the page is scrolled
        AJS.$(window).scroll(function () {
            hideDialog(false);
        });

    });
})(AJS.$);

