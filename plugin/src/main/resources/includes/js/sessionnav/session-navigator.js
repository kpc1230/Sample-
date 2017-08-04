AJS.$(function() {      
    /** jQuery Override for 1.5.2 fix in ie8 **/
    if (jQuery.fn.jquery == "1.5.2" && jQuery.browser.msie) {
        jQuery._Deferred.resolveWith = function( context, args ) {
            if ( !cancelled && !fired && !firing ) {
                // make sure args are available (#8421)
                args = args || [];
                firing = 1;
                try {
                    while( callbacks[ 0 ] ) {
                        callbacks.shift().apply( context, args );
                    }
                }
                catch(e) {}
                finally {
                    fired = [ context, args ];
                    firing = 0;
                }
            }
            return this;
        }
    }
    /** Placeholder fix for ie **/
    var generatePlaceholders = function($contents) {
        // Placeholder text for browsers that don't support the attribute (Firefox <= 3.6, IE)
        var placeholderTest = document.createElement('input');
        if (!('placeholder' in placeholderTest)) {
            var $placeholders = $contents.find('.bf-nav-placeholder');
            $placeholders.each(function () {
                var $this = jQuery(this),
                $label = jQuery('<label/>', {
                    'class': 'bf-nav-placeholder-label',
                    'for': this.id,
                    'text': $this.attr('placeholder'),
                    'css': {
                        fontFamily: $this.css('fontFamily'),
                        fontSize: $this.css('fontSize')
                    }
                }).insertBefore($this);
                // Bind handlers
                $label.click(function() {
                    $this.focus();
                });
                $this.focus(function () {
                    $label.hide();
                }).bind('blur change', function () {
                    $label.toggle(!jQuery(this).val().length);
                });
                if($this.val().length) {
                    $label.hide();
                }
            });        
        }
        placeholderTest = null;
    }
    generatePlaceholders(jQuery(document));
    
    /**
    * Get filter data
    **/
    var getFilterData = function() {
        return {
            sortField: jQuery('.bf-nav-sortfield').val(),
            sortOrder: jQuery('.bf-nav-sortorder').val(),
            projectFilter: jQuery('.bf-nav-project-filter').val(),
            projectFilterKey: jQuery('.bf-nav-project-filter option:selected').data('pkey'),
            assigneeFilter: jQuery('.bf-nav-assignee-filter').val(),
            statusFilter: jQuery('.bf-nav-status-filter').val(),
            searchTerm: jQuery('.bf-nav-search-box').val()
        }
    }

    /**
    * Initialise permalink
    **/
    var updatePermalink = function() {
        var filterData = getFilterData();
        url = contextPath + '/secure/SessionNavigator.jspa?sortField=' + encodeURIComponent(filterData.sortField) + 
            '&sortOrder=' + encodeURIComponent(filterData.sortOrder) +
            '&projectFilter=' + encodeURIComponent(filterData.projectFilterKey) +
            '&userFilter=' + encodeURIComponent(filterData.assigneeFilter) +
            '&statusFilter=' + encodeURIComponent(filterData.statusFilter) +
            '&searchTerm=' + encodeURIComponent(filterData.searchTerm);
            jQuery('.bf-nav-permalink')[0].href = url;
    }

    /**
    * Dialog Listener
    **/
    jQuery(document).bind("dialogContentReady", function (e, dialogInstance) {
        BF.bindCompleteSessionCommon(e, dialogInstance);
        // Submit handler for the complete dialog
        jQuery('#bonfire-complete-button').bind('click', function(e) {
            var $thisButton = jQuery(this);
            BF.bindCompleteSubmit(e, $thisButton, refreshSessionNav);
        });
    });
    
    /**
    * Quick Sessions
    **/
    var initialiseQuickSession = function () {
        jQuery.each(jQuery('.bf-nav-quicksession-trigger'), function() {
            var dialogId = 'bf-dialog';
            var currentTrigger = jQuery(this);
            BF.quickSessionCreate(currentTrigger, dialogId);            
        });
    }
    
    /**
    * Dropdown Menu
    **/
    var initialiseDropdownMenu = function () {
        try{
            AJS.Dropdown.create({
                trigger: jQuery(".bf-more-div .bf-nav-more-trigger"),
                content: jQuery(".bf-more-div .aui-list"),
                alignment: AJS.RIGHT
            });
        } catch (err) {
            // Ignore. It means we are on IE8 and an out-of-context error is thrown.
        }
    }

    /**
    * Dropdown Menu Links
    **/
    var standardAjaxOptions = { data: {inline: true, decorator: "dialog"} };
    var dialogFinishedHandler = function (data, xhr, textStatus, smartAjaxResult) {
        refreshSessionNav();
    }
    var doSmartAjax = function(url, reqType) {
        JIRA.SmartAjax.makeRequest({
            url:url,
            type: reqType,
            dataType: "json",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                refreshSessionNav();
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
		onDialogFinished: dialogFinishedHandler,
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
		onSuccessfulSubmit: dialogFinishedHandler,
		onUnSuccessfulSubmit: dialogFinishedHandler,
		onDialogFinished: dialogFinishedHandler,
		ajaxOptions: standardAjaxOptions,
		width: "100%"
	});

    new AJS.FormPopup({
        trigger: ".bf-nav-create-session-link",
        id: "capture-create-test-session",
        autoClose: true,
        onSuccessfulSubmit : dialogFinishedHandler,
        onDialogFinished : dialogFinishedHandler,
        ajaxOptions: standardAjaxOptions
    });

    /**
    * Spinner methods
    **/
    var spinnerSpec = { lines: 12, length: 8, width: 4, radius: 10, trail: 60, speed: 1.5, className: 'bf-spinner' }; // Copied from Stash
    var spinnerTarget = jQuery('.bf-nav-spinner-container')[0];
    var spinner;
    var startSpinner = function() {
        if (!spinner) {
            spinner = new Spinner(spinnerSpec).spin(spinnerTarget);
        }
    };
    var stopSpinner = function() {
        if (spinner) {
            spinner.stop();
            delete spinner;
            spinner = null;
        }
    }
        
    /** 
    * methods to override in the infinite scroller 
    **/
    var requestSessionsData = function(pageRequest) {
        startSpinner();
        var filterData = getFilterData();
        var startAt = jQuery('.bf-nav-nextstart').val();
        var url = contextPath + '/rest/bonfire/1.0/sessions/filtered?startAt=' + 
            encodeURIComponent(startAt) + '&sortField=' + encodeURIComponent(filterData.sortField) + 
            '&sortOrder=' + encodeURIComponent(filterData.sortOrder);
        if (filterData.projectFilter.length) {
            url += '&projectFilter=' + encodeURIComponent(filterData.projectFilter);
        }
        if (filterData.assigneeFilter.length) {
            url += '&userFilter=' + encodeURIComponent(filterData.assigneeFilter);
        }
        if (filterData.statusFilter.length) {
            url += '&statusFilter=' + encodeURIComponent(filterData.statusFilter);
        }
        if (filterData.searchTerm.length) {
            url += '&searchTerm=' + encodeURIComponent(filterData.searchTerm);
        }
        var xhr = JIRA.SmartAjax.makeRequest({
            url:url,
            type: 'GET',
            dataType: "json",
            contentType: "application/json"
        });
        return xhr;
    };

    var attachSessionsContent = function(data, attachmentMethod) {
        var sessionsArray = data.sessions;
        if (!sessionsArray.length) {
            var contents = jQuery('.bf-nav-empty-session-list-container');
            if (!!data.hasAny) {
                jQuery(BF.template.drawEmptySessionList()).appendTo(contents);
            } else {
                var params = {
                    contextPath: contextPath
                }
                jQuery(BF.template.drawWelcome(params)).appendTo(contents);
            }
        } else {
            var contents = jQuery('.bf-nav-session-list dl');
            for (session in sessionsArray) {
                var params = {
                    session: sessionsArray[session],
                    contextPath: contextPath,
                    xsrfToken: AJS.$('#atlassian-token').attr('content')
                }
                jQuery(BF.template.drawSingleSessionRow(params)).appendTo(contents);
            }
            if (!data.hasMore && data.totalFilteredCount > sessionsArray.length) {
                jQuery(BF.template.drawNoMoreSessions()).appendTo(contents);
            }
        }
        if (!data.hasMore) {
            suspendScroller();
        }
        jQuery('.bf-nav-nextstart').val(data.nextStart);
        initialiseQuickSession();
        initialiseDropdownMenu();
        stopSpinner();
    };
    
    var handleScrollerErrors = function() {/* ignoreeed */};

    /**
    * Methods for controlling the infinite scroller
    **/
    var suspendScroller = function () {
        if (scroller) {
            scroller.suspend();
        }
    };
    var resumeScroller = function () {
        if (scroller) {
            scroller.resume();
        }
    };
    
    /**
    * Filtering
    **/
    jQuery('.bf-nav-filter').live('change', function(e) {
        var currentSelect = jQuery(this);
        var container = currentSelect.closest('.bf-nav-filter-container');
        var value = container.find('.bf-nav-filter-value');
        value.text(currentSelect[0].options[currentSelect[0].selectedIndex].text);
        refreshSessionNav();
    });
    jQuery('.bf-nav-search-box').live('keypress', function(e) {
        if (e.keyCode == 13 && !e.ctrlKey && ! e.shiftKey) {
            refreshSessionNav();
        }
    });
    
    /**
    * Reset Filters
    **/
    jQuery('.bf-nav-reset-filters').live('click', function(e){
        jQuery('.bf-nav-sortfield').val('');
        jQuery('.bf-nav-sortorder').val('ASC');
        jQuery('.bf-nav-project-filter').val('');
        jQuery('.bf-nav-assignee-filter').val('');
        jQuery('.bf-nav-status-filter').val('');
        jQuery('.bf-nav-search-box').val('');
        updateDisplayState();
        refreshSessionNav();
    });

    /**
    * Sorting
    **/
    jQuery('.bf-nav-sortable').live('click', function(e) {
        var sortableDiv = jQuery(this);
        var currentSortOrder = jQuery('.bf-nav-sortorder').val();
        var currentSortField = jQuery('.bf-nav-sortfield').val();
        var sortField = sortableDiv.data('sort-field');
        // cleanup classes
        jQuery('.bf-ascending').removeClass('bf-ascending');
        jQuery('.bf-descending').removeClass('bf-descending');
        jQuery('.bf-nav-sorted').removeClass('bf-nav-sorted');
        // update classes and values
        sortableDiv.addClass('bf-nav-sorted');
        // update the sort field
        jQuery('.bf-nav-sortfield').val(sortField);
        if (currentSortField === sortField && currentSortOrder !== 'DESC') {
            jQuery('.bf-nav-sortorder').val('DESC');    
            sortableDiv.addClass('bf-descending');
        } else {
            jQuery('.bf-nav-sortorder').val('ASC');
            sortableDiv.addClass('bf-ascending');
        }
        refreshSessionNav();
    });
        
    /**
    * Update local storage
    **/
    var updateLocalStorage = function() {
        var filterData = getFilterData();
        BF.storage.put("bonfire.navigator.sortField", filterData.sortField);
        BF.storage.put("bonfire.navigator.sortOrder", filterData.sortOrder);
        BF.storage.put("bonfire.navigator.projectFilter", filterData.projectFilter);
        BF.storage.put("bonfire.navigator.assigneeFilter", filterData.assigneeFilter);
        BF.storage.put("bonfire.navigator.statusFilter", filterData.statusFilter);
        BF.storage.put("bonfire.navigator.searchTerm", filterData.searchTerm);
    }
    /**
    * Set values of filters from local storage - Only read the value from local storage if there were no query params
    **/
    var setFromLocalStorage = function() {
        var currentUrl = document.URL;
        if (currentUrl.indexOf('sortField=') !== -1 || currentUrl.indexOf('sortOrder=') !== -1 || currentUrl.indexOf('projectFilter=') !== -1 || 
            currentUrl.indexOf('userFilter=') !== -1 || currentUrl.indexOf('statusFilter=') !== -1 || currentUrl.indexOf('searchTerm=') !== -1 ) {
            return;
        }
        var sortField = BF.storage.get("bonfire.navigator.sortField");
        var sortOrder = BF.storage.get("bonfire.navigator.sortOrder");
        var projectFilter = BF.storage.get("bonfire.navigator.projectFilter");
        var assigneeFilter = BF.storage.get("bonfire.navigator.assigneeFilter");
        var statusFilter = BF.storage.get("bonfire.navigator.statusFilter");
        var searchTerm = BF.storage.get("bonfire.navigator.searchTerm");
        jQuery('.bf-nav-sortfield').val(sortField);
        jQuery('.bf-nav-sortorder').val(sortOrder);
        jQuery('.bf-nav-project-filter').val(projectFilter);
        jQuery('.bf-nav-assignee-filter').val(assigneeFilter);
        jQuery('.bf-nav-status-filter').val(statusFilter);
        jQuery('.bf-nav-search-box').val(searchTerm);
    }
        
    /**
    * Refresh navigator method
    **/
    var refreshSessionNav = function() {
        jQuery('.bf-nav-empty-session-list-container').empty();
        jQuery('.bf-nav-session-list dl').empty();
        jQuery('.bf-nav-nextstart').val(0);
        updatePermalink();
        resumeScroller();
        updateLocalStorage();
    }
    
    /** Ensure initial display state **/
    var updateDisplayState = function() {
        var currentSortOrder = jQuery('.bf-nav-sortorder').val();
        var currentSortField = jQuery('.bf-nav-sortfield').val();
        jQuery('.bf-nav-sortable').each(function() {
            var thisDiv = jQuery(this);
            var thisSortField = thisDiv.data('sort-field')
            if(thisSortField === currentSortField) {
                thisDiv.addClass('bf-nav-sorted');
                if ('DESC' === currentSortOrder) {
                    thisDiv.addClass('bf-descending');
                } else {
                    thisDiv.addClass('bf-ascending');
                }
            } else {
                thisDiv.removeClass('bf-ascending');
                thisDiv.removeClass('bf-descending');
                thisDiv.removeClass('bf-nav-sorted');
            }
        });
        jQuery('.bf-nav-filter-container').each(function(){
            var container = jQuery(this);
            var value = container.find('.bf-nav-filter-value');
            var currentSelect = container.find('.bf-nav-filter');
            value.text(currentSelect[0].options[currentSelect[0].selectedIndex].text);
        });
    }
    /** The method that you run to make sure the world is ready for the spinner **/
    var initialState = function() { 
        setFromLocalStorage();
        updateDisplayState();
        updateLocalStorage();
        updatePermalink();
    }
    initialState();
        
    /** initialise the infinite scroller. Always do this last **/
    var scroller = AJS.InfiniteScroller("body");
    scroller.requestData = requestSessionsData,
    scroller.attachNewContent = attachSessionsContent,
    scroller.handleErrors = handleScrollerErrors
    scroller.init();
});
