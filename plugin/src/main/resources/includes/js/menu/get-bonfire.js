AJS.$(function ($) {
    $('a.bf-track').bind('click', function(e) {
        var destination = this.href;
        var analytic = $(this).data('analytic');
        var analyticLabel = typeof analytic === 'undefined' ? destination : analytic;
        trackEvent('jira.capture.learn.more', {source: 'jira'});
    });
});
