jQuery(function ($) {
    if (jQuery('#bonfire_top_menu_dropdown').is(':visible')) {
        var calloutDialog = AJS.InlineDialog(jQuery('#bonfire_top_menu_dropdown'), 'bonfire-callout-dialog', function(contents, trigger, showPopup) {
            var $contents = jQuery(contents);
            $contents.append(BF.template.drawCallout());
            showPopup();
        },{
            noBind: true,
            hideDelay: null,
            getArrowPath: function (positions) {
                return "M11,0L0,8,11,16";
            },
            getArrowAttributes: function () {
                return {
                    fill: "#FFFFFF",
                    stroke: "#BBB"
                };
            },
            calculatePositions: function (popup, targetPosition, mousePosition, opts) {
                var targetOffset = targetPosition.target.offset();
                var isUnifiedHeader = jQuery('.aui-header').length !== 0;
                var verticalOffset = isUnifiedHeader ? 6 : 0;
                var horizontalOffset = targetPosition.target.parent().width() + 7;

                return {
                    displayAbove: false,
                    hideDelay: null,
                    popupCss: {
                        top: targetOffset.top + verticalOffset,
                        left: targetOffset.left + horizontalOffset,
                        right: "auto"
                    },
                    arrowCss: {
                        position: "absolute",
                        left: '-10px',
                        top: '5px'
                    }
                };
            }
        });
        // Remove unneeded click event handlers
        jQuery(document).bind("showLayer", function(e, type, hash) {
            if(type && type === "inlineDialog" && hash && hash.id && hash.id === 'bonfire-callout-dialog') {
                hash._validateClickToClose = function(e) {
                    if (jQuery(e.target).hasClass('bf-callout-cross')) {
                        // If the click is inside the dialog, then close it
                        return true;
                    }
                    // Don't close the dialog
                    return false;
                }
                jQuery("body").unbind("click." + hash.id + ".inline-dialog-check");
            }
        });
        
        setTimeout(function(){
            calloutDialog.show();
        }, 500);
    }
});