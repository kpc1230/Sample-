if (typeof BF == 'undefined') { window.BF = {}; }

AJS.$(function(){

    /******************
    * Local Variables
    *******************/

    var ARROW_HEIGHT_CONSTANT = 150;
    var dialogCounter = 0;// Used to ensure these dialogs have unique ids
    var $window = jQuery(window);
    var inlineQuickSessionOptions = {
        width: 430,
        hideDelay: null,
        getArrowPath: function (positions) {
            return "M0,0L11,8,0,16";
        },
        // Make the arrow point right
        calculatePositions: function (popup, targetPosition, mousePosition, opts) {
            var targetOffset = targetPosition.target.offset();
            var windowTop = $window.scrollTop();

            return {
                displayAbove: false,
                popupCss: {
                    position: "fixed",
                    top: targetOffset.top - ARROW_HEIGHT_CONSTANT - windowTop,
                    left: targetOffset.left - popup.width() - 11,
                    right: "auto"
                },
                arrowCss: {
                    position: "absolute",
                    left: popup.width() - 1,
                    right: "auto",
                    top: ARROW_HEIGHT_CONSTANT
                }
            };
        }
    };
        
    /******************
    * External Methods
    *******************/
    
    BF.quickSessionCreate = function(element, dialogId) {
        dialogId = dialogId + dialogCounter;
        // Make the readjust method
        var readjustDialog = function(e) {
            var windowTop = $window.scrollTop();
            var popup = jQuery('#inline-dialog-' + dialogId); 
            var currentTriggerOffset = element.offset();
            var newTopOffset = currentTriggerOffset.top - ARROW_HEIGHT_CONSTANT - windowTop;
            var newLeftOffset = currentTriggerOffset.left - popup.width() - 11;
            popup.css({
                top: newTopOffset,
                left: newLeftOffset
            });
        }
        jQuery.extend(inlineQuickSessionOptions, {
            initCallback: function(){
                $window.bind('scroll resize', readjustDialog);
            },
            hideCallback: function(){
                $window.unbind('scroll resize', readjustDialog);
            },                
        });
        // Make the dialog
        new AJS.InlineDialog(element, dialogId, function(contents, trigger, showPopup){
            var sessionId = jQuery(trigger).attr("data-session-id");
            var url = contextPath + '/rest/bonfire/1.0/quick-session/' + sessionId;
            JIRA.SmartAjax.makeRequest({
                url:url,
                type: "GET",
                dataType: "json",
                contentType: "application/json",
                complete: function (xhr, textStatus, smartAjaxResult) {
                    var $contents = jQuery(contents); 
                    $contents.empty();
                    if (smartAjaxResult.successful) {
                        var params = {
                            session: smartAjaxResult.data,
                            contextPath: contextPath
                        }
                        jQuery(BF.template.quicksession(params)).appendTo(contents);
                        generatePlaceholders(contents);
                    } else {
                        jQuery(BF.template.quicksessionError()).appendTo(contents);
                    }
                    showPopup();
                }
            });
        }, inlineQuickSessionOptions);
        // Remove unneeded click event handlers
        jQuery(document).bind("showLayer", function(e, type, hash) {
            if(type && type === "inlineDialog" && hash && hash.id && hash.id === dialogId) {
                hash._validateClickToClose = function(e) {
                    if (jQuery(e.target).parents('#inline-dialog-' + hash.id).length > 0) {
                        // If the click is inside the dialog, then don't close
                        return false;
                    }
                    // Close the dialog
                    return true;
                }
                jQuery("body").unbind("click." + hash.id + ".inline-dialog-check");
            }
        });
        dialogCounter++;
    };
    
    /******************
    * Internal Methods
    *******************/

    var generatePlaceholders = function($contents) {
        // Placeholder text for browsers that don't support the attribute (Firefox <= 3.6, IE)
        var placeholderTest = document.createElement('input');
        if (!('placeholder' in placeholderTest)) {
            var $placeholders = $contents.find('.bfq-placeholder');
            $placeholders.each(function () {
                var $this = jQuery(this),
                $label = jQuery('<label/>', {
                    'class': 'bfq-placeholder-label',
                    'for': this.id,
                    'text': $this.attr('placeholder'),
                    'css': {
                        fontFamily: $this.css('fontFamily'),
                        fontSize: $this.css('fontSize')
                    }
                }).insertBefore($this);
                // Bind handlers
                $this.focus(function () {
                    $label.hide();
                }).bind('blur change', function () {
                    $label.toggle(!jQuery(this).val().length);
                });
            });        
        }
        placeholderTest = null;
    }
    
    var refreshButtons = function(element) {
        jQuery('.bfq-button').addClass('bfq-not-selected');
        jQuery(element).removeClass('bfq-not-selected');
    }
    
    var scrollContainerToBottom = function ($container) {
        $container.animate({
            scrollTop: $container.scrollTop() + $container.height()
        }, 500);
    }
    
    var deselectAdditionalInfoEdit = function ($container) {
        $container.find('.bfq-addinfo-edit').addClass('hidden');
        $container.find('.bfq-addinfo-wiki').removeClass('hidden');
        $container.find('.bfq-error').empty();
        $container.find('.bfq-info-textarea').unbind('blur', onAddInfoBlurSubmit);
    }

    var deselectNoteEdit = function ($container) {
        var $renderedNote = $container.find('.bfq-note-display-container');
        var $noteEditContainer = $container.find('.bfq-note-edit');
        $renderedNote.removeClass('hidden');
        $noteEditContainer.addClass('hidden');
        $container.find('.bfq-edit-error').empty();
        $container.find('.bfq-note-textarea').unbind('blur', onNoteBlurSubmit);
        // note deselected - re-enable the various click events
        jQuery('.bfq-edit-note').live('click', selectNoteEdit);
    }
    
    var selectNoteEdit = function(e) {
        e.preventDefault();
        var $container = jQuery(this).closest('.bfq-note-container');
        var $renderedNote = $container.find('.bfq-note-display-container');
        var $noteEditContainer = $container.find('.bfq-note-edit');
        var originalNote = $container.find('.bfq-original-note').val();
        var $noteTextarea = $container.find('.bfq-note-textarea');
        $noteTextarea.val(originalNote);
        $renderedNote.addClass('hidden');
        $noteEditContainer.removeClass('hidden');
        $noteTextarea.focus();
        $noteTextarea.bind('blur', onNoteBlurSubmit);
        // note selected - disable all the other note click events
        jQuery('.bfq-edit-note').die('click', selectNoteEdit);
    }
    
    var cancelAddInfoBlurSubmit = function(e) {
        var $container = jQuery(this).closest('.bfq-additionalInfo');
        var timeoutObject = $container.data('blurTimeout');
        if (typeof timeoutObject != 'undefined') {
            clearTimeout(timeoutObject);
            $container.find('.bfq-info-textarea').focus();
        }
    }

    var cancelNoteBlurSubmit = function(e) {
        var $container = jQuery(this).closest('.bfq-note-container');
        var timeoutObject = $container.data('blurTimeout');
        if (typeof timeoutObject != 'undefined') {
            clearTimeout(timeoutObject);
            $container.find('.bfq-note-textarea').focus();
        }
    }
    
    var onNoteBlurSubmit = function(e) {
        var $container = jQuery(this).closest('.bfq-note-container');
        var timeoutObject = setTimeout(function(){
            $container.find('.bfq-note-edit-actions .bfq-submit').click();
        }, 100); 
        // Add timeout object that *may* be cancelled by the cancel button
        $container.data('blurTimeout', timeoutObject);
    }
    
    var onAddInfoBlurSubmit = function(e) {
        var $container = jQuery(this).closest('.bfq-additionalInfo');
        var timeoutObject = setTimeout(function(){
            $container.find('.bfq-add-info-actions .bfq-submit').click();
        }, 100); 
        // Add timeout object that *may* be cancelled by the cancel button
        $container.data('blurTimeout', timeoutObject);
    }
    
    var drawErrorMessages = function(smartAjaxResult, $errorContainer) {
        var errorArray = JSON.parse(smartAjaxResult.data).errors; 
        for (error in errorArray) {
            var errorMessage = errorArray[error].errorMessage;
            if (typeof errorMessage !== "undefined") {
                $errorContainer.append('<span>' + errorMessage + '</span><br/>');
            }
        }
    }
  
    /*************
    * Live events
    **************/
            
    //// Tabs ////
    
    jQuery('.bfq-addinfo-tab').live('click', function(e) {
        jQuery('.bfq-swappable-content').addClass('hidden');
        jQuery('.bfq-additionalInfo').removeClass('hidden');
        refreshButtons(this);
    });

    jQuery('.bfq-notes-tab').live('click', function(e) {
        jQuery('.bfq-swappable-content').addClass('hidden');
        jQuery('.bfq-notes').removeClass('hidden');
        refreshButtons(this);
    });
    
    //// Additional Info ////
    
    jQuery('.bfq-addinfo-wiki.bfq-editable').live('click', function(e) {
        var $target = jQuery(this);
        var $container = $target.closest('.bfq-additionalInfo');
        $target.addClass('hidden');
        var $editdiv = $container.find('.bfq-addinfo-edit');
        $editdiv.removeClass('hidden');
        var originalData = $container.find('.bfq-original-addinfo').val();
        var $textArea = $editdiv.find('.bfq-info-textarea');
        $textArea.val(originalData);
        $textArea.focus();

        $container.find('.bfq-info-textarea').bind('blur', onAddInfoBlurSubmit);
    });

    jQuery('.bfq-add-info-actions .bfq-cancel').live('click', function(e) {
        e.preventDefault();
        var $container = jQuery(this).closest('.bfq-additionalInfo');
        deselectAdditionalInfoEdit($container);
    });
    jQuery('.bfq-add-info-container').live('keydown', function(e) {
        if(e.keyCode == 27){
            e.preventDefault();
            var $container = jQuery(this).closest('.bfq-additionalInfo');
            deselectAdditionalInfoEdit($container);
        }
        if (e.ctrlKey && (e.keyCode == 13 || e.keyCode == 10)) {
            e.preventDefault();
            var $container = jQuery(this).closest('.bfq-additionalInfo');
            $container.find('.bfq-add-info-actions .bfq-submit').click();
        }
    });
    
    jQuery('.bfq-add-info-actions').live('click', cancelAddInfoBlurSubmit);
    jQuery('.bfq-add-info-container').live('click', cancelAddInfoBlurSubmit);
    
    jQuery('.bfq-add-info-actions .bfq-submit').live('click', function(e) {
        var $container = jQuery(this).closest('.bfq-additionalInfo');
        var $origAddInfo = $container.find('.bfq-original-addinfo');
        var $wikiAddInfo = $container.find('.bfq-addinfo-wiki');
        var $addInfoError = $container.find('.bfq-error');
        var $spinner = $container.find('.bfq-waiting-icon');
        var sessionId = $container.find('.bfq-session-id').val();
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/additionalInfo';
        var additionalInfo = $container.find('.bfq-info-textarea').val();
        var data = {
            additionalInfo: additionalInfo
        }; 
        $addInfoError.empty();
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
                        $wikiAddInfo.html(BF.template.getNoAddInfoMsg());
                    }
                    else
                    {
                        $wikiAddInfo.html(smartAjaxResult.data.additionalInfoDisplay);
                    }
                    $origAddInfo.text(smartAjaxResult.data.additionalInfoRaw);
                    deselectAdditionalInfoEdit($container);
                }
                else
                {
                    drawErrorMessages(smartAjaxResult, $addInfoError);
                }
            }
        });
    });
    
    //// Notes ////
    
    jQuery('.bfq-add-note-textarea').live('focus', function(){
        var $container = jQuery(this).closest('.bfq-notes');
        $container.find('.bfq-note-button').removeClass('hidden');
        scrollContainerToBottom($container);
    });

    jQuery('.bfq-add-note-textarea').live('keydown', function(e) {
        if (e.ctrlKey && (e.keyCode == 13 || e.keyCode == 10)) {
            e.preventDefault();
            var $container = jQuery(this).closest('.bfq-notes');
            $container.find('.bfq-note-button').click();
        }
    });

    jQuery('.bfq-note-button').live('click', function() {
        var $container = jQuery(this).closest('.bfq-notes');
        var sessionId = $container.find('.bfq-session-id').val();
        var $addNoteArea = $container.find('.bfq-add-note-textarea');
        var newNote = $addNoteArea.val();
        var $addNoteError = $container.find('.bfq-error.bfq-addnote-error');
        var $allNotesContainer = $container.find('.bfq-allnotes');
        var url = contextPath + '/rest/bonfire/1.0/sessions/' + sessionId + '/note';
        var data = {
            note: newNote
        }; 
        $addNoteError.empty();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) 
                {
                    $addNoteArea.val('');
                    $allNotesContainer.find('em').remove();
                    $allNotesContainer.append(BF.template.drawnote({
                        note: smartAjaxResult.data
                    }));
                }
                else
                {
                    drawErrorMessages(smartAjaxResult, $addNoteError);
                }
                scrollContainerToBottom($container);
            }
        });
    });
    
    jQuery('.bfq-delete-note').live('click', function(e) {
        e.preventDefault();
        // TODO: i18n once it's available for plugins
        if (confirm('Are you sure you want to delete this session note?')) {
            var $container = jQuery(this).closest('.bfq-note-container');
            var $allNotesContainer = $container.closest('.bfq-allnotes');
            var noteId = $container.find('.bfq-note-id').val();
            var $delNoteError = $container.find('.bfq-error');
            var url = contextPath + '/rest/bonfire/1.0/notes/' + noteId;
            $delNoteError.empty();
            JIRA.SmartAjax.makeRequest({
                url:url,
                type: "DELETE",
                dataType: "json",
                contentType: "application/json",
                complete: function (xhr, textStatus, smartAjaxResult) {
                    if (smartAjaxResult.successful) 
                    {
                        $container.remove();
                        var noteCount = $allNotesContainer.find('.bfq-note-container').size();
                        if (!noteCount) {
                            $allNotesContainer.html(BF.template.getNoNoteMsg());
                        }
                    }
                    else
                    {
                        drawErrorMessages(smartAjaxResult, $delNoteError);
                    }
                }
            });
        }
    });
    
    jQuery('.bfq-note-data').live('click', function(e) {
        var $container = jQuery(this).closest('.bfq-note-container');
        $container.find('.bfq-edit-note').click();
    });
    
    jQuery('.bfq-edit-note').live('click', selectNoteEdit);
    
    jQuery('.bfq-note-edit-actions').live('click', cancelNoteBlurSubmit);
    jQuery('.bfq-note-edit-container').live('click', cancelNoteBlurSubmit);
    
    jQuery('.bfq-note-edit-actions .bfq-cancel').live('click', function(e) {
        var $container = jQuery(this).closest('.bfq-note-container');
        deselectNoteEdit($container);
    });
    
    jQuery('.bfq-note-edit-container').live('keydown', function(e) {
        if(e.keyCode == 27){
            e.preventDefault();
            var $container = jQuery(this).closest('.bfq-note-container');
            deselectNoteEdit($container);
        }
        if (e.ctrlKey && (e.keyCode == 13 || e.keyCode == 10)) {
            e.preventDefault();
            var $container = jQuery(this).closest('.bfq-note-container');
            $container.find('.bfq-note-edit-actions .bfq-submit').click();
        }
    });

    jQuery('.bfq-note-edit-actions .bfq-submit').live('click', function(e) {
        var $container = jQuery(this).closest('.bfq-note-container');
        var noteId = $container.find('.bfq-note-id').val();
        var url = contextPath + '/rest/bonfire/1.0/notes/' + noteId;
        var $editNoteError = $container.find('.bfq-edit-error');
        var noteData = $container.find('.bfq-note-textarea').val();
        var $wikiNote = $container.find('.bfq-note-data');
        var $origNote = $container.find('.bfq-original-note');
        var $spinner = $container.find('.bfq-waiting-icon');
        var data = {
            noteData: noteData
        }; 
        $editNoteError.empty();
        $spinner.removeClass('hidden');
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "PUT",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                $spinner.addClass('hidden');
                if (smartAjaxResult.successful) 
                {
                    $wikiNote.html(smartAjaxResult.data.noteData);
                    $origNote.text(smartAjaxResult.data.rawNoteData);
                    deselectNoteEdit($container);
                }
                else
                {
                    drawErrorMessages(smartAjaxResult, $editNoteError);
                }
            }
        });
    });
});