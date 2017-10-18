/**
 * Created by aliakseimatsarski on 6/12/17.
 */
var ZEPHYR = ZEPHYR || {};
ZEPHYR.ADG = ZEPHYR.ADG || {};
ZEPHYR.ADG.CONFIG = ZEPHYR.ADG.CONFIG || {};

ZEPHYR.ADG.CONFIG.init = function (userKey) {
    AJS.dialog2("#demo-dialog").show();
    ZEPHYR.ADG.CONFIG.getAdgFlag(userKey, function (adgFlag) {
        var $checkbox = AJS.$('#adg-flag-chbx');
        if(adgFlag){
            $checkbox.attr('checked', 'checked');
        }
        $checkbox.removeAttr('disabled');
        $checkbox.change(function () {
            if($(this).is(":checked")) {
                console.log("checked");
                ZEPHYR.ADG.CONFIG.setAdgFlag(userKey, true);
            } else {
                ZEPHYR.ADG.CONFIG.setAdgFlag(userKey, false);
            }
        })
    })
};

ZEPHYR.ADG.CONFIG.getAdgFlag = function (userKey, success) {
    AP.require('request', function(request) {
        request({
            url: '/rest/api/2/user/properties/adg-flag?userKey=' + userKey + '&_' + new Date().getTime(),
            type: 'GET',
            contentType: 'application/json',
            success: function (response) {
                var response = JSON.parse(response);
                var flag = response.value.adg;
                success && success(flag);
            },
            error: function () {
                success && success(false);
            }
        });
    });
};

ZEPHYR.ADG.CONFIG.setAdgFlag = function (userKey, flag, success, error) {
    AP.require('request', function(request) {
        request({
            url: '/rest/api/2/user/properties/adg-flag?userKey=' + userKey,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({adg: flag}),
            success: function () {
                ZEPHYR.ADG.CONFIG.clearServerCache();
                success && success();
            },
            error: function (errorMessage) {
                error && error(errorMessage);
            }
        });
    });
};

ZEPHYR.ADG.CONFIG.clearServerCache = function () {
    AJS.$.ajax({
        url:  '/capture/private/admin/adg/cache',
        type: "DELETE",
        success: function(){
            console.log('ADG flag server cache cleared');
        },
        error: function () {
            console.error('ADG flag server cache error');
        }
    })
};
