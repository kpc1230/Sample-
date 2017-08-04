if (typeof BF == 'undefined') { window.BF = {}; }
if (typeof BF.gadget == 'undefined') { BF.gadget = {}; }
if (typeof BF.gadget.fields == 'undefined') { BF.gadget.fields = {}; }

BF.gadget.fields.userPicker = function(gadget, userUserPref, contextPath) {
    return {
        userpref: userUserPref,
        label: "User",
        id: userUserPref,
        type: "callbackBuilder",
        callback: function (parentDiv)
        {
            var $input = bfjQueryoverride("<input type='text' />").attr({
                id: userUserPref,
                name: userUserPref,
                value: gadget.getPref(userUserPref)
            }).addClass("user").autocomplete({
                source: function(request, response) {
                    jQuery.getJSON(contextPath + "/rest/bonfire/latest/userSearch", request, function(data) {
                        response(data.searchResult);
                    });
                },
                minLength: 1,
                height: "10px"
            });

            parentDiv.append($input);
        }
    }
};

/**
* getValue - method to get the value out of the array
* getText - method to get the text out of the array
**/
window.createFilterOption = function(gadget, idKey, array, getValue, getText, parentDiv) {
    var currentVal = gadget.getPref(idKey);
    var $select = jQuery('<select name="' + idKey + '"/>');
    jQuery('<option value="">All</option>').appendTo($select);
    jQuery.each(array, function(index, value) {
        var thisValue = getValue(value);
        var thisText = getText(value);
        var optionString = '<option value="' + gadgets.util.escapeString(thisValue) + '"';
        if ( currentVal === gadgets.util.escapeString(thisValue) ) {
            optionString = optionString + ' selected="selected"';
        }
        optionString = optionString + '>' + gadgets.util.escapeString(thisText) + '</option>';
        var option = jQuery(optionString);
        option.appendTo($select);
    });
    parentDiv.append($select);
}

BF.gadget.fields.userFilterDropdowns = function(gadget, data, contextPath) {
    var idKey = 'bonfire-user-filter';
    return {
        userpref: idKey,
        label: 'Assignee',
        id: idKey,
        type: 'callbackBuilder',
        bfdata: data,
        callback: function (parentDiv)
        {
            var userArray = this.bfdata.filterOptions.users;
            createFilterOption(gadget, idKey, userArray, function(value) {
                return value.value;
            }, function(value) {
                return value.text;
            }, parentDiv);
        }
    }
};

BF.gadget.fields.projectFilterDropdowns = function(gadget, data, contextPath) {
    var idKey = 'bonfire-project-filter';
    return {
        userpref: idKey,
        label: 'Project',
        id: idKey,
        type: 'callbackBuilder',
        bfdata: data,
        callback: function (parentDiv)
        {
            var projectArray = this.bfdata.filterOptions.projects;
            createFilterOption(gadget, idKey, projectArray, function(value) {
                return value.id;
            }, function(value) {
                return value.name;
            }, parentDiv);
        }
    }
};

BF.gadget.fields.statusFilterDropdowns = function(gadget, data, contextPath) {
    var idKey = 'bonfire-status-filter';
    return {
        userpref: idKey,
        label: 'Status',
        id: idKey,
        type: 'callbackBuilder',
        bfdata: data,
        callback: function (parentDiv)
        {
            var statusArray = this.bfdata.filterOptions.statuses;
            createFilterOption(gadget, idKey, statusArray, function(value) {
                return value.value;
            }, function(value) {
                return value.text;
            }, parentDiv);
        }
    }
};
