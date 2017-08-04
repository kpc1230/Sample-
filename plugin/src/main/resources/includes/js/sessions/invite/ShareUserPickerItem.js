// Copied from JIRA
/**
 * A JIRA.ShareUserPicker.Item represents an item selected in the Share Dialog.
 * It is much like an AJS.MultiSelect.Lozenge but is rendered differently with slightly altered behaviour.
 *
 * @constructor JIRA.ShareUserPicker.Item
 * @extends AJS.Control
 */
JIRA.ShareUserPicker.Item = AJS.Control.extend({

    init: function(options) {
        this._setOptions(options);

        this.$lozenge = this._render("item");
        this.$removeButton = this.$lozenge.find('.remove-recipient');

        this._assignEvents("instance", this);
        this._assignEvents("lozenge", this.$lozenge);
        this._assignEvents("removeButton", this.$removeButton);

        this.$lozenge.appendTo(this.options.container);
    },

    _getDefaultOptions: function() {
        return {
            label: null,
            title: null,
            container: null,
            focusClass: "focused"
        };
    },

    _renders: {
        "item": function() {
            var descriptor = this.options.descriptor;
            if (descriptor.noExactMatch() !== true) {
                // A User selected from the matches
                var username = descriptor.value();
                var icon = descriptor.icon();
                var displayName = AJS.escapeHtml(descriptor.label());

                return AJS.$('<li data-username="' + username +'" title='+ username +'>'
                      +  '<span>'
                      +    '<img src="' + icon + '" title="' + displayName + '">'
                      +     '<span title="' + displayName + '">' + displayName + '</span>'
                      +     '<span class="remove-recipient item-delete"/>'
                      +   '</span>'
                      +'</li>');
            } else {
                // Just an email
                var email = AJS.escapeHtml(descriptor.value());
                var icon = AJS.$('#bonfire-default-avatar-url').val();
                return AJS.$('<li data-email="' + email + '" title="' + email + '">'
                      +  '<span>'
                      +    '<img src="' + icon + '" title="' + email + '">'
                      +    '<span title="' + email + '">' + email + '</span>'
                      +    '<span class="remove-recipient item-delete"/>'
                      +  '</span>'
                      +'</li>');
            }
        }
    },

    _events: {
        "instance": {
            "remove": function() {
                this.$lozenge.remove();
            }
        },
        "removeButton": {
            "click": function(e) {
                // We need to stop the click propagation, else by the time the InlineDialog catches the event the span
                // will no longer be in the DOM and the click handler will think the user clicked outside of the dialog,
                // closing it.
                e.stopPropagation();
                this.trigger("remove");
            }
        }
    }
});
