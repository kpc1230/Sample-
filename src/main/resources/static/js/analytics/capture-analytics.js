// Atlassian Analytics
AJS.trigger = AJS.trigger || function() {};

window.prepAccountInfo = function(){
    // TODO: drop this GA Tracking code
    var trackingCode = jQuery('#bonfire-build-info').find('[data-bonfire-var="bonfire-gaTrackingCode"]').data('bonfire-value');
}

jQuery(prepAccountInfo);

/**
 * Tracking JIRA events using client-side tracking APIs if available
 * NOTE: works only if tracking is switched on by admin
 *
 * @param name event name
 * @param propertiesJson properties in JSON
 * @since 2.8.1
 */
window.trackEvent = function(name, propertiesJson) {
    AJS.trigger('analyticsEvent', { name: name, data: propertiesJson});
}

window.getBonfire = function(browser){
    trackEvent('jira.capture.install.extension', {source : 'JIRA', browser : browser});
}

window.sessionStatusChangeInJira = function(status){
    trackEvent('jira.capture.session.status.change', {source : 'JIRA', status : status});
}

window.noteStatusChangeInJira = function(status){
    trackEvent('jira.capture.session.notes', {source : 'JIRA', status : status});
}

window.trackHelpButtonUse = function(){
    trackEvent('jira.capture.session.notes.help', {source : 'JIRA'});
}

window.trackParticipationLeave = function(){
    trackEvent('jira.capture.session.leave', {source : 'JIRA'});
}

window.trackParticipationJoin = function(){
    trackEvent('jira.capture.session.join', {source : 'JIRA'});
}
