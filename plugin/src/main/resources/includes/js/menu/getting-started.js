AJS.$(function(){
    jQuery('.bf-main-container a').bind('click', function(e) {
        var destination = this.href;
        var analytic = jQuery(this).data('analytic');
        var analyticLabel = typeof analytic === 'undefined' ? destination : analytic;
        trackEvent('jira.capture.get.started', {source: 'JIRA'});
    });
});