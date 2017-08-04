jQuery(function ($) {
    var joinHoverDialog = AJS.InlineDialog($('.bf-disabled-join'), 'join-hover-dialog', function(contents, trigger, showPopup){
        var $contents = jQuery(contents);
        $contents.empty();
        $contents.append(jQuery('.bf-disabled-join-reason').clone().removeClass('hidden')).show();
        jQuery(trigger).bind('mouseleave', function(){
            joinHoverDialog.hide();
        });
        showPopup();
    }, {
        onHover: true,
        fadeTime: 200
    });

    var $addNoteInput = $(".session-add-note-input"),
        $addNoteSubmit = $(".session-add-note-submit"),
        activeClass = "active";

    // BON-660: The hasFixedParent data is to stop expandOnInput() from snapping back to the form after pressing Page Up
    // It also has to be added after the expandOnInput() call to avoid being overwritten
    $addNoteInput.expandOnInput().data('hasFixedParent', true).bind("focus", function () {
        $addNoteInput.addClass(activeClass);
        $addNoteSubmit.addClass(activeClass);
    });

    // if we have #addNote on the url then we want to focus the addNote field
    if (window.location.hash == "#newNote") {
        $("#newNote").trigger('focus');
    }

    // we may also get instructions to focus specifically via a hidden div
    var focusSelector = $("#focusTo").text();
    if (focusSelector) {
        $(focusSelector).trigger('focus');
    }
    
    var shareRestRequester = function(e, reqType){
        e.preventDefault();
        var $shareButton = jQuery(this);
        var sessionId = jQuery('#bonfire-session-id').val();
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/shared';
        var $errorBox = jQuery('#bf-opsbar-errors');
        $errorBox.addClass('hidden');
        $errorBox.find("p").remove();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: reqType,
            dataType: "json",
            data: '[]',
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    location.reload();
                }
                else {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#bf-opsbar-errors').append('<p>' + errorArray[error].errorMessage + '</p>');
                    }
                    $errorBox.removeClass('hidden');
                }
            }
        });
    };

    jQuery('#share-test-session').bind('click', function(e){
        shareRestRequester(e, "POST");
    });

    jQuery('#unshare-test-session').bind('click', function(e){
        shareRestRequester(e, "DELETE");
    });

    jQuery('#addNoteForm').bind('submit', function(e){
        var $createForm = jQuery(this);
        var sessionId = $createForm.find('[name=testSessionId]').val();
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/note';
        var noteData = $createForm.find('textarea').val();
        var data = {
            note: noteData
        }; 
        jQuery('#error-new-note').empty();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    $createForm.addClass("ajs-dirty-warning-exempt");
                    noteStatusChangeInJira('Create');
                    trackEvent('jira.capture.session.notes.add', {source : 'JIRA'});
                    $createForm.find('.session-add-note-input-container').html('');
                    location.reload();
                }
                else {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#error-new-note').append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        });
        return false;
    });
    
    /**
    * Additional Info stuff
    */
    var cancelAddInfoBlurSubmit = function(e) {
        var $additionalInfoForm = jQuery(this).closest('.edit-additional-info-form');
        var timeoutObject = $additionalInfoForm.data('blurTimeout');
        if (typeof timeoutObject != 'undefined') {
            clearTimeout(timeoutObject);
            $additionalInfoForm.find('#session-info-textarea').focus();
        } 
    };
    var onAddInfoSubmit = function(e) {
        var $addinfoForm = jQuery(this).closest('.edit-additional-info-form');
        var timeoutObject = setTimeout(function(){
            $addinfoForm.submit();
        }, 100); 
        // Add timeout object that *may* be cancelled by the cancel button
        $addinfoForm.data('blurTimeout', timeoutObject);
    };

    function deselectAdditionalInfo() {
        jQuery('#session-additional-info-edit').addClass('hidden');
        jQuery('#session-additional-info-rendered').removeClass('hidden');
        jQuery('#additional-info-target.editable .icon-edit-sml').removeClass('hidden');
        jQuery('#additionalInfo-errors').empty();
        jQuery('#session-info-textarea').unbind('blur', onAddInfoSubmit);
    }
    
    function editAdditionalInfo() {
        jQuery('#session-additional-info-rendered').addClass('hidden');
        jQuery('#additional-info-target.editable .icon-edit-sml').addClass('hidden');
        var $editBox = jQuery('#session-additional-info-edit').removeClass('hidden');
        var original = $editBox.find('#session-additional-info-original').val();
        var $textarea = $editBox.find('#session-info-textarea');
        $textarea.val(original);
        // RESET HEIGHT
        $textarea.height(0);
        $textarea.expandOnInput();
        $textarea.focus().triggerHandler("input");
        jQuery('#session-info-textarea').bind('blur', onAddInfoSubmit);
        $editBox.bind('keydown', function(e) {
            if (e.keyCode == 27) {
                deselectAdditionalInfo();
            }
        });
    }
    
    jQuery('#additional-info-target.editable #session-additional-info-rendered').bind('click', function (e) {
        var $target = $(e.target);
        // Don't do anything if the user clicked on a link
        if ($target.closest('a, #session-additional-info').is('a')) {
            return;
        }
        editAdditionalInfo();
    });
    jQuery('#additional-info-target.editable .icon-edit-sml').bind('click', function (e) {
        editAdditionalInfo();
    });    
    jQuery("#session-additional-info-buttons .bf-cancel").bind("click", function(e) {
        e.preventDefault();
        deselectAdditionalInfo();
    });
    
    // the wrapper around the additional info gaining focus should not cause the form to submit
    jQuery('.textbox-wrapper').bind('click', cancelAddInfoBlurSubmit);
    // The buttons should not cause the form to submit on blur
    jQuery('#session-additional-info-buttons').bind('click', cancelAddInfoBlurSubmit);

    jQuery('.edit-additional-info-form').bind('submit', function(e){
        var $additionalInfoForm = jQuery(this);
        var $spinner = $additionalInfoForm.find('.bf-waiting-icon');
        var sessionId = jQuery('#bonfire-session-id').val();
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/additionalInfo';
        var additionalInfo = $additionalInfoForm.find('#session-info-textarea').val();
        var data = {
            additionalInfo: additionalInfo
        }; 
        jQuery('#additionalInfo-errors').empty();
        $spinner.removeClass('hidden');
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                $spinner.addClass('hidden');
                if (smartAjaxResult.successful) 
                {
                    if(smartAjaxResult.data.isEmpty) 
                    {
                        jQuery('#session-additional-info-rendered').addClass('bf-empty');
                    } 
                    else
                    {
                        jQuery('#session-additional-info-rendered').removeClass('bf-empty');
                    }
                    jQuery('#session-additional-info-rendered').html(smartAjaxResult.data.additionalInfoDisplay);
                    jQuery('#session-additional-info-original').text(smartAjaxResult.data.additionalInfoRaw);
                    deselectAdditionalInfo();
                }
                else 
                {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#additionalInfo-errors').append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        });
        return false;
    });

    // Keyboard shortcut - type 'e' to edit session
    var $editButton = $('#edit-test-session');
    if ($editButton.length) {
        AJS.whenIType('e').click($editButton);
    }

    var initFancyBoxForClass = function(aClassName)
    {
        var closeFancyBox = function()
        {
            jQuery(aClassName).fancybox.close();
        };
        var isFireFoxLinux = function()
        {
            return jQuery.os.linux && jQuery.browser.mozilla;
        };
        var useOverlay = true;
        // FF on Linux looks like a car accident when the overlay is applied.  Its all over the place
        // like a mad womans breakfast.  So lets opt out for that combination.  FF in Windows/M<ac is not affected
        if (isFireFoxLinux())
        {
            useOverlay = false;
        }
        var fancyBoxOptions = {
            'imageScale' : true,
            'centerOnScroll' : false,
            'overlayShow': useOverlay,
            //looks like this isn't used a all??
            callbackOnStart : function ()
            {
                jQuery("#header").css("zIndex", "-1");
                if (useOverlay) {
                    jQuery("body").addClass("fancybox-show");
                }
            },
            //looks like this isn't used a all??
            'callbackOnShow' : function()
            {
                jQuery(document).click(function()
                {
                    closeFancyBox();
                });
            },
            'onComplete' : function()
            {
                // fix up title lozenge placement for narrow images (JRADEV-1797)
                var title = AJS.$('#fancybox-title');
                var mainWidth = AJS.$('#fancybox-title-main').outerWidth();
                var leftWidth = AJS.$("#fancybox-title-left").outerWidth();
                var rightWidth = AJS.$("#fancybox-title-right").outerWidth();

                title.width(mainWidth + leftWidth + rightWidth + 5);

                var imageDivWidth = AJS.$('#fancybox-inner').width();
                title.css("marginLeft", -(title.width() / 2) + (imageDivWidth / 2));
                title.css('bottom', title.outerHeight(true) * -1);
            },
            //looks like this isn't used a all??
            'callbackOnClose' : function()
            {
                jQuery("#header").css("zIndex", "");
                if (useOverlay) {
                    jQuery("body").removeClass("fancybox-show");
                }
                jQuery(document).unbind('click', closeFancyBox);
                if (jQuery.browser.safari) {
                    var top = AJS.$(window).scrollTop();
                    AJS.$(window).scrollTop(10 + 5 * (top == 10)).scrollTop(top);
                }
            }
        };
        if(AJS.$.browser.msie) {
            fancyBoxOptions.transitionIn = 'none';
            fancyBoxOptions.transitionOut = 'none';
        }
        jQuery(aClassName).fancybox(fancyBoxOptions);
    };
    
    initFancyBoxForClass("a.bf-gallery");
    
    /**
    * More Actions Menu
    **/
    var initMoreActionsMenu = function () {
        try{
            AJS.Dropdown.create({
                trigger: jQuery(".bf-opts-dropdown .bf-opts-dropdown-trigger"),
                content: jQuery(".bf-opts-dropdown .aui-list"),
                alignment: AJS.RIGHT
            });
        } catch (err) {
            // Ignore. It means we are on IE8 and an out-of-context error is thrown.
        }
    }
    initMoreActionsMenu();
});
