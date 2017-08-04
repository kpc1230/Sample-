AJS.$(function() {
        // Issue Collector - Code mostly taken from GH
    AJS.$.ajax({
        url: "https://jira.atlassian.com/s/en_UKsx8qk0/710/2/1.0.8-beta/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js",
        type: "get",
        cache: true,
        dataType: "script",
        timeout: 2000, // if JAC is taking too long to respond, then we just won't bother with feedback
        success: function() {
            var collectJiraVersion = function() {
                var jiraBuildInfo = AJS.$("#footer-build-information").text();
                // regex match for the version strings
                if (jiraBuildInfo) {
                    var m = jiraBuildInfo.match(/v(.*?)(?=\))/);
                    if (m.length > 1) {
                        // take the sub-match
                        return m[1];
                    }
                    // if fail, fall back to the full string
                    return jiraBuildInfo;
                }
                return "";
            };
            
            var collectBonfireVersion = function(){
                return AJS.$('#bonfire-build-info dt[data-bonfire-var="bonfire-buildVersion"]').data('bonfire-value');
            };

            AJS.$(function() {
                new ATL_JQ.IssueDialog({
                    collectorId:"cfd06cab",
                    collectFeedback: function() {
                        return {
                            "Location":             window.location,
                            "User-Agent":           navigator.userAgent,
                            "Referrer":             document.referrer,
                            "Screen resolution":    screen.width + " x " + screen.height,
                            "JIRA version":         collectJiraVersion(),
                            "Bonfire version":      collectBonfireVersion()
                        };
                    },
                    triggerPosition: function(onClickFn) {
                        // bind our feedback link to be the trigger of the form
                        AJS.$(document).delegate("#bonfire-feedback", "click", function(e){
                            e.preventDefault();
                            onClickFn();
                        });
                    }
                });
            });
        }
    });
});
