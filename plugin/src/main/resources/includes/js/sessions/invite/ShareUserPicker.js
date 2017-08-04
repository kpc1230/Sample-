//Backport from JIRA
(function($) {

var emailExpression = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;

/**
 * A multi-select list for selecting recipients to Share an issue or filter with. Shares are to 2 types of recipients:
 * - Users: selected from a dropdown list, and
 * - Email: addresses typed out in full
 *
 * @constructor JIRA.ShareUserPicker
 * @extends AJS.MultiSelect
 */
JIRA.ShareUserPicker = AJS.MultiSelect.extend({

    init: function (options) {

        var restPath = "/rest/api/1.0/users/picker";

        function formatResponse(response) {

            var ret = [];

            $(response).each(function(i, suggestions) {

                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: i, // order or groups in suggestions dropdown
                    label: suggestions.footer // Heading of group
                });

                $(suggestions.users).each(function(){
                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                        value: this.name, // value of item added to select
                        label: this.displayName, // title of lozenge
                        html: this.html,
                        icon: this.avatarUrl,
                        allowDuplicate: false
                    }));
                });

                ret.push(groupDescriptor);
            });

            return ret;
        }

        $.extend(options, {
            itemAttrDisplayed: "label",
            // can not make the js translation work, the transformer picks the wrong resource bundle
            userEnteredOptionsMsg: 'Email address',
            showDropdownButton: false,
            removeOnUnSelect: true,
            ajaxOptions: {
                url: contextPath + restPath,
                query: true,                // keep going back to the server for each keystroke
                data: { showAvatar: true },
                formatResponse: formatResponse
            },
            itemGroup: new AJS.Group(),
            itemBuilder: function (descriptor) {
                return new JIRA.ShareUserPicker.Item({
                    descriptor: descriptor,
                    container: this.$selectedItemsContainer
                });
            }
        });

        this._super(options);
    },

    hasUserInputtedOption: function () {
        var entry = $.trim(this.$field.val());
        return emailExpression.test(entry);
    },

    /**
     * The share textarea has no lozenges inside it and no need for cursor and indent nonsense.
     * It could even be a plain text field.
     */
    updateItemsIndent: function () {},

    _renders: {
        selectedItemsWrapper: function () {
            return $('<div class="recipients"></div>');
        },
        selectedItemsContainer: function () {
            return $('<ol />');
        }
    }

});

}(AJS.$));