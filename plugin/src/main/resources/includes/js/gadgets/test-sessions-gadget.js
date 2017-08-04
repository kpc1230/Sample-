GADGET = {};

/**
 * Ajax requests to be executed before calling template()
 */
GADGET.templateArgs = function(baseUrl) {
    return [ {
        key: "bfTestSessionsGadget",
        ajaxOptions: function()
        {
            var user = this.getPref("bonfire-user-filter");
            var project = this.getPref("bonfire-project-filter");
            var status = this.getPref("bonfire-status-filter");
            var url = baseUrl + "/rest/bonfire/1.0/sessions/filtered?startAt=0&size=20";
            if (AJS.$.trim(user).length !== 0) {
                url = url + "&userFilter=" + user
            }
            if (AJS.$.trim(project).length !== 0) {
                url = url + "&projectFilter=" + project
            }
            if (AJS.$.trim(status).length !== 0) {
                url = url + "&statusFilter=" + status
            }

            return {
                url: url
            }
        }
    }];
};

//this is the view template, which is rendered after the dashboard delivers your ajax results
GADGET.template = function (gadget, args, baseUrl) {
    gadget.getView().empty();
    var $sessions = AJS.$('<div class="bf-gadget-rows-container"/>');
    var sessionsArg =  args.bfTestSessionsGadget.sessions;
    if (sessionsArg.length === 0) {
        AJS.$(BF.gadget.template.drawEmptyState()).appendTo($sessions);
    } else {
        var $dl = AJS.$('<dl style="margin:0;" />');
        AJS.$.each(sessionsArg, function(idx, session) {
            var params = {
                session: session,
                contextPath: baseUrl
            };
            AJS.$(BF.gadget.template.drawSingleSession(params)).appendTo($dl);
        });
        $dl.appendTo($sessions);
        var user = gadget.getPref("bonfire-user-filter");
        var project = gadget.getPref("bonfire-project-filter");
        var status = gadget.getPref("bonfire-status-filter");
        var url = baseUrl + '/secure/SessionNavigator.jspa?sortField=""';
        if (AJS.$.trim(user).length !== 0) {
            url = url + "&userFilter=" + user
        }
        if (AJS.$.trim(project).length !== 0) {
            url = url + "&projectFilter=" + project
        }
        if (AJS.$.trim(status).length !== 0) {
            url = url + "&statusFilter=" + status
        }
        var params = {
            numDisplayed: sessionsArg.length,
            numMatched: args.bfTestSessionsGadget.totalFilteredCount,
            sessionURL: url
        }
        AJS.$(BF.gadget.template.drawGadgetFooter(params)).appendTo($sessions);
    }

    gadget.getView().append($sessions);
};


GADGET.descriptorArgs = function (baseUrl) {
    return [ {
        key: "filterOptions",
        ajaxOptions: function()
        {
            var url = baseUrl + "/rest/bonfire/1.0/gadgets/testsessions/filterInfo";
            return {
                url: url
            }
        }
    }];
};

//defines any preferences. 
GADGET.descriptor = function (gadget, args, baseUrl) {
    return {
        fields: [
            BF.gadget.fields.userFilterDropdowns(gadget, args, baseUrl),
            BF.gadget.fields.projectFilterDropdowns(gadget, args, baseUrl),
            BF.gadget.fields.statusFilterDropdowns(gadget, args, baseUrl),
            AJS.gadget.fields.nowConfigured()
        ]
    }
};


