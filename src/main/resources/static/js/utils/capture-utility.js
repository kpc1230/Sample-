var ZEPHYR = ZEPHYR || {};
ZEPHYR.CaptureUtils = ZEPHYR.CaptureUtils || {};

ZEPHYR.CaptureUtils.gdprBannerCheck = function(token) {
	AJS.$.ajax({
        url: "/capture/isGdpr",
        type : "GET",
        headers: {
            'Authorization': 'JWT ' + token
        },
        contentType :"application/json",
        success : function(response) {
            if(response && response.hasOwnProperty('isGDPR') && !response.isGDPR) {
                var msg = '<strong>Warning !!!</strong>  - Despite several email reminders, we have not been able to migrate your Capture For Jira Cloud instance due to missing permission. As Atlassian is removing UserKey and UserName from their REST API started Monday 29th April 2019, Capture For Jira Cloud will need to be migrated to keep functioning correctly.<strong> Please have your admin perform the following operation before May 2, 2019 12 PM PST to avoid being blocked. </strong><br/><br/><strong><font size="3">** Add the "atlassian-addons-admin" user to \'Global permission\' -> \'Browse users and groups\' group. </font></strong><br/><br/><strong>On Saturday May 4, 2019 7 AM PST - A new build will be deployed blocking access to the Capture For Jira Cloud application.</strong> The application will continue to be blocked until JIRA admin trigger the In App migration which can take anytime between an hour to 8 hours depending on the data (during this time, Capture For Jira use will continue to be blocked). Prevent downtime, have your admin act and provide the permission.';
                AJS.$('body').prepend('<div class="gdpr-banner aui-banner aui-banner-error">'+ msg +'</div>');
            }
        }
    });
}
