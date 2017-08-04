jQuery(function ($) {
    var cancelEditNoteBlurSubmit = function(e) {
        var $noteForm = jQuery(this).closest('.editable-note-target');
        var timeoutObject = $noteForm.data('blurTimeout');
        if (typeof timeoutObject != 'undefined') {
            clearTimeout(timeoutObject);
            $noteForm.find('.editable-note-input').focus();
        }
    }
    var onEditNoteSubmit = function(e) {
        var $currentForm = jQuery(this).closest('.editable-note-target');
        var timeoutObject = setTimeout(function(){
            $currentForm.submit();
        }, 100); 
        // Add timeout object that *may* be cancelled by the cancel button
        $currentForm.data('blurTimeout', timeoutObject);
    }

    function checked($e, val) {
        if (val !== undefined) {
            $e.attr('checked', !!val);
        }
        return $e.attr('checked');
    }

    function disabled($e, val) {
        if (val !== undefined) {
            $e.attr('disabled', !!val);
        }
        return $e.attr('disabled');
    }

    function initial($e) {
        var b = $e.attr("data-initial");
        if (b === undefined) {
            return "";
        }
        return b;
    }

    var filterInit = false,
        $filterForm = $('#bf-filter-form.notes');

    var filterOptions = {
        hideCallback: function() {
            enforceInitialState();
            $filterForm.removeData(AJS.DIRTY_FORM_VALUE);
        }
    };

    function disableSpecifics(disable) {
        var $specifics = $("#filter-specifics.notes"),
            $inputs = $specifics.find("input:checkbox");

        $specifics.toggleClass("disabled", disable);
        disabled($inputs, disable);
    }

    // if we started out as filter all and now have moved to filter applied
    // and there are none selected then choose question as a default
    var selectQuestionIfNoneSelected = function () {
        var $filterNothing = $("#notesFilterNothing");
        if (initial($filterNothing)) {
            var someSelected = false;
            var $inputs = $("#filter-specifics.notes")
                .find("input:checkbox")
                .each(function() {
                    var $cb = $(this);
                    someSelected = someSelected || checked($cb);
                });
            if (! someSelected) {
                checked($("#notesFilterQuestion"),true);
            }
        }
    };


    var enforceInitialState = function() {

        var $filterNothing = $('#notesFilterNothing');
        var $filterApplied = $("#notesFilterApplied");
        var $filterStatus = $("#notesFilterStatus");

        checked($filterNothing, initial($filterNothing));
        checked($filterApplied, initial($filterApplied));
        disableSpecifics(! initial($filterApplied));

        var $inputs = $("#filter-specifics.notes")
            .find("input:checkbox")
            .each(function() {
                var $cb = $(this);
                checked($cb, initial($cb));
            });

        var status = $filterStatus.find("option[data-initial]").val();
        $filterStatus.val(status);
    };

    var $sessionsLink = $('#session-filter.notes');
    var filterDialog = AJS.InlineDialog($sessionsLink, 'session-notes-filter', function (contents, trigger, showPopup) {
        if (!filterInit) {
            $filterForm.appendTo(contents).show().click(function (e) {
                e.stopPropagation();
            });
            $filterForm.removeClass('hidden');
            $('#filter-close').click(function (e) {
                e.preventDefault();
                filterDialog.hide();
            });
            filterInit = true;
        }
        showPopup();
        enforceInitialState();
    }, filterOptions);

    // this manages the state of the inputs so they at radio button like
    // in that filterNothing overrides more specific filtering
    $filterForm.delegate('input', 'click', function() {

        var $clicked = $(this);
        var $filterNothing = $("#notesFilterNothing");
        var $filterApplied = $("#notesFilterApplied");

        if ($clicked.attr('id') == 'notesFilterNothing') {
            if (checked($filterNothing)) {
                checked($filterApplied, false);
                disableSpecifics(true);
            }
        }
        if ($clicked.attr('id') == 'notesFilterApplied') {
            if (checked($filterApplied)) {
                checked($filterNothing, false);
                disableSpecifics(false);
                selectQuestionIfNoneSelected();
            } else {
                checked($filterNothing, true);
                disableSpecifics(true);
            }
        }
    });

    function buildFilterFormQuery($form) {
        var params = $form.formToArray(),
            i = params.length,
            currentParam;
        while (i--) {
            currentParam = params[i];
            if (currentParam.name == "notesFilterStatus") {
                var val = currentParam.value,
                    all = val == "all";
                if (all || val == "complete") {
                    params.push({name: "notesFilterComplete", value: "true"});
                }
                if (all || val == "incomplete") {
                    params.push({name: "notesFilterIncomplete", value: "true"});
                }
                params.splice(i, 1);
                break;
            }
        }
        return $.param(params);
    }

    $filterForm.submit(function () {
        // Cancel any "dirty form" warnings
        $filterForm.removeData(AJS.DIRTY_FORM_VALUE);

        var queryString = buildFilterFormQuery($filterForm);
        var url = contextPath + $filterForm.attr('data-filer-url') + "&" + queryString;
        window.location.replace(url);
        // Prevent normal browser submit
        return false;
    });


    /** ---------------------------- */

    var $flashed = jQuery(".editable-note-target.flash");
    $flashed.delay(700).animate(
        {
            backgroundColor: "#ffffff"
        }, 1800,
        function() {
            $flashed.removeClass('flash');
            $flashed.css('backgroundColor', '');
        });
    var currentTarget;

    function deselectCurrentNote() {
        if (currentTarget) {
            currentTarget.removeClass("active");
            currentTarget.find(".editable-note-input-container").removeClass("active");
            currentTarget.find(".editable-note-buttons").addClass("hidden");
            currentTarget.find(".editable-note-readonly").addClass("active");
            currentTarget.find(".warning").empty();
            // Cancel any "dirty form" warnings
            currentTarget.closest("form").removeData(AJS.DIRTY_FORM_VALUE);
            var $textarea = currentTarget.find(".editable-note-input");
            $textarea.unbind('blur', onEditNoteSubmit);

            currentTarget = null;
        }
    }

    function buildDynamicInputContentFor(noteInputContainer) {
        var parentContainer = noteInputContainer.closest('.editable-note-content');
        var noteId = parentContainer.attr('data-note-id');
        var sessionId = parentContainer.attr('data-session-id');
        noteInputContainer.html(
            '<input type="hidden" name="noteId" value="' + noteId + '"> ' +
                '<input type="hidden" name="testSessionId" value="' + sessionId + '"> ' +
                '<textarea class="editable-note-input" name="noteText"></textarea>' +
                '<span class="bf-note-waiting-icon hidden"></span>'
        );

        noteInputContainer.bind('keydown', function(e) {
            if (e.keyCode == 27) {
                deselectCurrentNote();
            }
        });

        var $textarea = noteInputContainer.find(".editable-note-input");
        // Use JIRA's helper function for submitting a form with ctrl+enter
        $textarea.keypress(submitOnCtrlEnter);

        return $textarea;

    }

    function startEditingNote($noteTarget) {
        deselectCurrentNote();

        var $noteReadOnly = $noteTarget.find(".editable-note-readonly");
        var $noteInputContainer = $noteTarget.find(".editable-note-input-container");
        var $noteButtons = $noteTarget.find(".editable-note-buttons");
        var $noteOriginal = $noteTarget.find(".editable-note-original");
        var $noteInput = buildDynamicInputContentFor($noteInputContainer);

        $noteInput.val($noteOriginal.val());

        $noteInput.expandOnInput();

        $noteReadOnly.removeClass("active");
        $noteInputContainer.addClass("active");
        $noteButtons.removeClass("hidden");
        $noteTarget.addClass("active");

        $noteInput.focus().triggerHandler("input");
        $noteInput.bind('blur', onEditNoteSubmit);

        currentTarget = $noteTarget;

    }


    // click handler for readonly text to editable text
    jQuery(".editable-note-readonly.editable").bind("click", function (e) {
        var $target = $(e.target);
        // Don't do anything if the user clicked on a link in the note, or any element inside a link
        if ($target.is('span.editable-note-delete-button') || $target.closest('a, .editable-note-readonly').is('a')) {
            return;
        }

        startEditingNote($(this).closest('.editable-note-target'));
    });

    jQuery(".editable-note-buttons .bf-cancel").bind("click", function(e) {
        e.preventDefault();
        deselectCurrentNote();
    });
    
    // the wrapper around the note gaining focus should not cause the form to submit
    jQuery('.editable-note-input-container').bind('click', cancelEditNoteBlurSubmit);
    jQuery('.editable-note-buttons').bind('click', cancelEditNoteBlurSubmit);

    jQuery(".bf-toggle-note-link").bind("click", function(e) {
        e.preventDefault();
        var $spanLink = jQuery(this);
        var noteId = $spanLink.attr('data-note-id');
        var url = contextPath + "/rest/bonfire/1.0/notes/" + noteId + "/toggleResolution";
        var $editableNoteDiv = jQuery('#session-note-' + noteId);
        jQuery('#error-' + noteId).empty();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: "[]",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    var noteClass = 'completed';
                    if (smartAjaxResult.data.resolutionState === "COMPLETED") {
                        noteStatusChangeInJira('Complete');
                        $editableNoteDiv.addClass(noteClass);
                        $spanLink.find('a').text('Reopen');
                    } else {
                        noteStatusChangeInJira('Uncomplete');
                        $editableNoteDiv.removeClass(noteClass);
                        $spanLink.find('a').text('Complete');
                    }
                } else {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#error-' + noteId).append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        })
    });
    
    jQuery(".bf-edit-note-link").bind("click", function(e) {
        e.preventDefault();
        e.stopPropagation(); 
        var $spanLink = jQuery(this);
        var noteId = $spanLink.attr('data-note-id');
        var $editableNoteDiv = jQuery('#session-note-' + noteId);
        startEditingNote($editableNoteDiv);
    });
    
    jQuery(".bf-delete-note-link").bind("click", function(e) {
        e.preventDefault();
        // TODO: i18n once it's available for plugins
        if (confirm('Are you sure you want to delete this session note?')) {
            var $spanLink = jQuery(this);
            var noteId = $spanLink.attr('data-note-id');
            noteStatusChangeInJira('Delete');
            jQuery('#note-delete-form-' + noteId).submit();
        }
    });
    
    jQuery(".editable-note-delete-button").bind("click", function(e) {
        // TODO: i18n once it's available for plugins
        if (confirm('Are you sure you want to delete this session note?')) {
            e.preventDefault();
            var $spanButton = jQuery(this);
            var $parentContainer = $spanButton.closest('.editable-note-content');
            var noteId = $parentContainer.attr('data-note-id');
            noteStatusChangeInJira('Delete');
            jQuery('#note-delete-form-' + noteId).submit();
        }
    });
    
    jQuery(".edit-note-form").bind("submit", function(e) {
        var $editForm = jQuery(this);
        var noteId = $editForm.attr('data-note-id');
        var url = contextPath + "/rest/bonfire/1.0/notes/" + noteId;
        var returnUrl = contextPath + $editForm.find('[name=returnUrl]').val();
        var $noteWikiContainer = $editForm.find('.editable-note-wiki-wrapper');
        var $noteOriginal = $editForm.find('.editable-note-original');
        var noteData = $editForm.find('.editable-note-input').val();
        var $spinner = $editForm.find('.bf-note-waiting-icon');
        var data = {
            noteData: noteData
        }; 
        jQuery('#error-' + noteId).empty();
        $spinner.removeClass('hidden');
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "PUT",
            dataType: "json",
            data: BF.utils.bonJSONStringify(data),
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                $spinner.addClass('hidden');
                if (smartAjaxResult.successful) {
                    $noteWikiContainer.html(smartAjaxResult.data.noteData);
                    $noteOriginal.text(smartAjaxResult.data.rawNoteData);
                    if (smartAjaxResult.data.resolutionState === 'NON_ACTIONABLE') {
                        jQuery('#bf-toggle-' + noteId).addClass('hidden');
                        $editForm.find('.editable-note-target').removeClass('completed');
                    } else {
                        jQuery('#bf-toggle-' + noteId).removeClass('hidden');
                    }
                    noteStatusChangeInJira('Update');
                    deselectCurrentNote();
                } else {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#error-' + noteId).append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        });
        return false;
    });

    jQuery(".editable-note-resolution-control").bind("click", function(e) {
        var $resolutionControl = jQuery(this);
        var noteId = $resolutionControl.attr('data-note-id');
        var url = contextPath + "/rest/bonfire/1.0/notes/" + noteId + "/toggleResolution";
        var $noteContainer = $resolutionControl.closest('.editable-note-target');
        jQuery('#error-' + noteId).empty();
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: "POST",
            dataType: "json",
            data: "[]",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    var noteClass = 'completed';
                    // editing a note bumps its id so we need to reflect this
                    $resolutionControl.attr('data-note-id', smartAjaxResult.data.id);
                    if (smartAjaxResult.data.resolutionState === "COMPLETED") {
                        noteStatusChangeInJira('Complete');
                        $noteContainer.addClass(noteClass);
                    } else {
                        noteStatusChangeInJira('Uncomplete');
                        $noteContainer.removeClass(noteClass);
                    }
                } else {
                    var errorArray = JSON.parse(smartAjaxResult.data).errors; 
                    for (error in errorArray) {
                        jQuery('#error-' + noteId).append('<span>' + errorArray[error].errorMessage + '</span><br/>');
                    }
                }
            }
        })
    });

    // Start editing a note that has an error message
    var $editError = $(".editable-note-target .bonfire-error");
    if ($editError) {
        startEditingNote($editError.closest(".editable-note-target"));
    }

});
