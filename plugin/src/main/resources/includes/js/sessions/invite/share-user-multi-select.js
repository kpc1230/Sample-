(function($) {

    function createShareUserPicker(ctx) {
        $(".share-user-picker", ctx).each(function () {
            var control = new JIRA.ShareUserPicker({
                element: $(this)
            });
            $(document).trigger('ready.multi-select.share-user', control);
        });
    }

    JIRA.bind('bonfireInviteReady', function (e, context) {
        createShareUserPicker(context);
    });

}(AJS.$));


// TODO - some of this code will be reused for the free-email UI.
JIRA.SharePage = {
    autocompleteUser: function(scope) {
        scope = scope || document.body;
        var $ = AJS.$,
                emailExpression = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;

        var makeRestMatrixFromData = function (restObj) {
            if (!restObj || !restObj.result){
                throw new Error("Invalid JSON format");
            }
            var matrix = [];
            matrix.push(restObj.result);
            return matrix;
        };

        $("input.autocomplete-sharepage[data-autocomplete-user-bound!=true]", scope).each(function() {
            var $this = $(this)
                    .attr("data-autocomplete-sharepage-bound", "true")
                    .attr("autocomplete", "off");
            var maxResults = $this.attr("data-max") || 10,
                    alignment = $this.attr("data-alignment") || "left",
                    dropDownTarget = $this.attr("data-dropdown-target"),
                    dropDownPosition = null;

            if (dropDownTarget) {
                dropDownPosition = $(dropDownTarget);
            }
            else {
                dropDownPosition = $("<div></div>");
                $this.after(dropDownPosition);
            }
            dropDownPosition.addClass("aui-dd-parent autocomplete");

            $this.quicksearch(AJS.REST.getBaseUrl() + "search/user.json",
                    function() {
                        $this.trigger("open.autocomplete-sharepage");
                    }, {
                        makeParams : function(val) {
                            return {
                                "max-results": maxResults,
                                query: val
                            };
                        },
                        dropdownPlacement: function(dd) {
                            dropDownPosition.append(dd);
                        },
                        makeRestMatrixFromData : makeRestMatrixFromData,
                        addDropdownData : function (matrix) {
                            if (emailExpression.test($this.val())) {
                                matrix.push([{
                                    name: $this.val(),
                                    email: $this.val(),
                                    href: "#",
                                    icon: AJS.Confluence.getContextPath() + "/images/icons/profilepics/anonymous.png"
                                }]);
                            }

                            if (!matrix.length) {
                                var noResults = $this.attr("data-none-message");
                                if (noResults) {
                                    matrix.push([{
                                        name: noResults,
                                        className: "no-results",
                                        href: "#"
                                    }]);
                                }
                            }

                            return matrix;
                        },
                        ajsDropDownOptions : {
                            alignment: alignment,
                            displayHandler: function(obj) {
                                if (obj.restObj && obj.restObj.username) {
                                    return obj.name + " (" + obj.restObj.username + ")";
                                }
                                return obj.name;
                            },
                            selectionHandler: function (e, selection) {

                                if (selection.find(".search-for").length) {
                                    $this.trigger("selected.autocomplete-sharepage", { searchFor: $this.val() });
                                    return;
                                }
                                if (selection.find(".no-results").length) {
                                    this.hide();
                                    e.preventDefault();
                                    return;
                                }

                                var contentProps = $("span:eq(0)", selection).data("properties");

                                if (!contentProps.email) {
                                    contentProps = contentProps.restObj;
                                }

                                $this.trigger("selected.autocomplete-sharepage", { content: contentProps });
                                this.hide();
                                e.preventDefault();
                            }
                        }
                });
        });
    }
};