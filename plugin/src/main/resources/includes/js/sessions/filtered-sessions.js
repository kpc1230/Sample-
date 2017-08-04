AJS.$(function () {
    var $ = AJS.$;

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

    function disableSpecifics(disable) {
        var $specifics = $("#filter-specifics.sessions"),
            $inputs = $specifics.find("input:checkbox");

        $specifics.toggleClass("disabled", disable);
        disabled($inputs, disable);
    }

    // if we started out as filter all and now have moved to filter applied
    // and there are none selected then choose question as a default
    var selectionIfNoneSelected = function () {
        var $filterNothing = $("#sessionsFilterNothing");
        if (initial($filterNothing)) {
            var someSelected = false;
            var $inputs = $("#filter-specifics.sessions")
                .find("input:checkbox")
                .each(function() {
                    var $cb = $(this);
                    someSelected = someSelected || checked($cb);
                });
            if (! someSelected) {
                checked($("#sessionsFilterStatusCreated"),true);
            }
        }
    };


    var enforceInitialState = function() {

        var $filterNothing = $('#sessionsFilterNothing');
        var $filterApplied = $("#sessionsFilterApplied");
        var $filterStatus = $("#sessionsFilterStatus");

        checked($filterNothing, initial($filterNothing));
        checked($filterApplied, initial($filterApplied));
        disableSpecifics(! initial($filterApplied));

        var $inputs = $("#filter-specifics.sessions")
            .find("input:checkbox")
            .each(function() {
                var $cb = $(this);
                checked($cb, initial($cb));
            });

        var status = $filterStatus.find("option[data-initial]").val();
        $filterStatus.val(status);
    };

    function buildFilterFormQuery($form) {
        var params = $form.formToArray();
        return $.param(params);
    };

    var initBonfireFilterDialog = function () {
        while($('#inline-dialog-session-sessions-filter').length){
            $('#inline-dialog-session-sessions-filter').remove();
        }
        var filterInit = false;
    
        var filterOptions = {
            useLiveEvents: true
        };
    
        var $sessionsLink = $('#session-filter.sessions');
        var filterDialog = AJS.InlineDialog($sessionsLink, 'session-sessions-filter', function (contents, trigger, showPopup) {
            $filterForm = $('#bf-filter-form.sessions');
            if (!filterInit && $filterForm.length !== 0) {
                initSessionFilterForm(contents, trigger, showPopup, $filterForm);
                filterInit = true;
            }
            showPopup();
            enforceInitialState();
        }, filterOptions);

        var initSessionFilterForm = function (contents, trigger, showPopup, $filterForm) {
            $(contents).empty();
            $filterForm.appendTo(contents).show().click(function (e) {
                e.stopPropagation();
            });
            $filterForm.removeClass('hidden');
            $('#filter-close').click(function (e) {
                e.preventDefault();
                filterDialog.hide();
            });
    
            // this manages the state of the inputs so they at radio button like
            // in that filterNothing overrides more specific filtering
            $filterForm.delegate('input', 'click', function() {
        
                var $clicked = $(this);
                var $filterNothing = $("#sessionsFilterNothing");
                var $filterApplied = $("#sessionsFilterApplied");
        
                if ($clicked.attr('id') == 'sessionsFilterNothing') {
                    if (checked($filterNothing)) {
                        checked($filterApplied, false);
                        disableSpecifics(true);
                    }
                }
                if ($clicked.attr('id') == 'sessionsFilterApplied') {
                    if (checked($filterApplied)) {
                        checked($filterNothing, false);
                        disableSpecifics(false);
                        selectionIfNoneSelected();
                    } else {
                        checked($filterNothing, true);
                        disableSpecifics(true);
                    }
                }
            });
        
            $filterForm.submit(function () {
                // Cancel any "dirty form" warnings
                $filterForm.removeData(AJS.DIRTY_FORM_VALUE);
        
                var queryString = buildFilterFormQuery($filterForm);
                var url = contextPath + $filterForm.attr('data-filer-url') + "&" + queryString;
                window.location.replace(url);
                // Prevent normal browser submit
                return false;
            });

            filterDialog.hideCallback = function () {
                enforceInitialState();
                $filterForm.removeData(AJS.DIRTY_FORM_VALUE);
            }
        };    
    };

    initBonfireFilterDialog();
});
