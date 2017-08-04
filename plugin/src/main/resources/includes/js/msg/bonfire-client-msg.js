jQuery.namespace("Bonfire.Messages");

/**
 * We cant yet use JIRA 5.0 new JIRA.Message goodness so we do this instead
 * @param msgInstructions
 */
Bonfire.Messages.showMsg = function (msgInstructions) {
    var positionMsg = function($container) {
        var bodyWidth = jQuery("body").width();
        var $header = jQuery("#header");
        var top = $header.offset().top + $header.outerHeight() + 10;

        $container.width('auto');
        $container.css( {
            width: 'auto',
            left: (bodyWidth - $container.width()) / 2,
            top: top,
            margin: 0,
            padding: '1em'
        });
    };

    var $msgTarget = jQuery(msgInstructions.target || "#session-top-bar");
    var $container = jQuery("<div>");
    $container.addClass("bonfire-msg aui-message " + msgInstructions.type.toLowerCase());


    $container.html(msgInstructions.msg);
    $container.prependTo($msgTarget);
    positionMsg($container);

    var animProps = {
        opacity: 0.2
    };
    $container.delay(700).animate(animProps, 2000, function () {
            $container.remove();
        }
    );
};
