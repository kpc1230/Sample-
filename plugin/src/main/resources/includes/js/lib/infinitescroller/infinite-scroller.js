(function(exports, console, $, LoadedRange, triggerFunc) {

    var isIE = $.browser.msie;


    /**
     * An abstract widget that will handle scroll events and load new data as the user scrolls to the end of the content.
     *
     * To extend InfiniteScroller, you must implement:
     * this.requestData(pageRequest) : given a pageRequest from AJS.LoadedRange, must return a promise with a RestPage object as the first done() argument.
     * this.attachNewContent(data, attachmentMethod) : given your retrieved data and an attachmentMethod ('prepend', 'append', or 'html'),
     *              should add new content somewhere within the element specified by scrollPaneSelector (where exactly is up to you)
     * this.handleErrors : will be passed the result of your requestData promise's fail handler and should handle that case.
     *
     * @param scrollPaneSelector - the element with (overflow: auto | scroll) which contain the pages of items
     * @param options see <code>InfiniteScroller.defaults</code>.
     */
    function InfiniteScroller(scrollPaneSelector, options) {
        if (!(this instanceof InfiniteScroller)) {
            return new InfiniteScroller(scrollPaneSelector, options);
        }

        this.options = $.extend({}, InfiniteScroller.defaults, options);

        this.$scrollElement = $(scrollPaneSelector || window);

        // Use window instead of document or body.
        if (this.$scrollElement[0] === document || this.$scrollElement[0] === document.body) {
            this.$scrollElement = $(window);
        }

        if ($.isWindow(this.$scrollElement[0])) {
            // we still want to attach to window.scroll, but documentElement has the properties we need to look at.
            var docEl = window.document.documentElement;
            this.getPaneHeight = function() { return docEl.clientHeight; };
            this.getContentHeight = function() { return docEl.scrollHeight; };
        }
    }
    /**
     * pageSize: used as the limit parameter to requestData,
     * loadAutomatically : When true, the next/previous page will be loaded as soon as the user scrolls it into view. Set this false if you prefer to trigger loads with a button.
     *              To trigger a load manually, call InfiniteScroller.load(pageRequest, shouldLoadAbove)
     * bufferPixels: load more data if the user scrolls within this many pixels of the edge of the loaded data.
     * scrollDelay: the number of milliseconds to debounce before handling a scroll event.
     * precedingSpaceMaintained: Set this to true if your implementation will add blank space above the loaded content as a placeholder for content
     *              that will be loaded as the user scrolls up.
     *
     *              Setting this to <code>true</code> means InfiniteScroller will load a previous page when
     *              the proportion of the content scrolled is less than the start of your loaded range (i.e., wandering into the 'placeholder' territory you've created.)
     *              <code>scrollElement.scrollTop < bufferPixels + (loadedRange.start / loadedRange.end) * scrollContent.height</code>
     *
     *              Setting it <code>false</code> means a previous page will be loaded when
     *              <code>scrollElement.scrollTop < bufferPixels</code>
     * suspendOnFailure : When enabled, the infinite-scroller will enter a suspended mode, as if InfiniteScroller.suspend() was called after a data request fails.
     *              To resume requesting data, call InfiniteScroller.resume().
     * eventBus : a function for triggering events, of the form function(string eventName, InfiniteScroller context, ...arguments);
     */
    InfiniteScroller.defaults = {
        pageSize: 50,
        scrollDelay : 250,
        bufferPixels : 0,
        precedingSpaceMaintained : true,
        suspendOnFailure : true,
        loadAutomatically : true,
        eventBus : triggerFunc
    };

    //load the initial data
    InfiniteScroller.prototype.init = function(targetedItem, loadedRange) {
        InfiniteScroller.prototype.reset.call(this);

        this.loadedRange = loadedRange || new AJS.LoadedRange(this.options.pageSize);

        var self = this,
            pageSize = this.options.pageSize;

        // if the start item is already loaded we probably don't have to load any more.
        if (this.loadedRange.isLoaded(targetedItem)) {

            // but it's possible the window is larger than the page size, so trigger a fake scroll anyway just to see if that causes any new loads.
            return (this.loadIfRequired() || $.Deferred().resolve()).done(function() {
                // do our onFirstDataLoaded call now if we can, or after whatever loads next.
                self.onFirstDataLoaded();
            });
        }

        return loadInternal(this, this.loadedRange.pageFor(targetedItem, pageSize), false).fail(function() {
            if (!self._cancelling) {
                self.handleErrors.apply(self, arguments);
            }
        });
    };

    InfiniteScroller.prototype.reset = function() {
        if (this.currentXHR) {
            this.cancelRequest();
        }

        this.clearScrollListeners();
        $(window).unbind('resize', this._resizeHandler);
        this._resizeHandler = null;

        // must happen after this.cancelRequest() to avoid the scrollable becoming suspended on a reinit.
        this._suspended = false;
    };

    /**
     * Stop requesting new data.  Any requests already in the pipeline will complete.
     * To resume requesting data, call InfiniteScroller.resume();
     */
    InfiniteScroller.prototype.suspend = function () {
        this._suspended = true;
    };

    /**
     * Resume requesting new data.
     */
    InfiniteScroller.prototype.resume = function () {
        this._suspended = false;

        // if they are near the top/bottom of the page, request the data they need immediately.
        this.loadIfRequired();
    };

    InfiniteScroller.prototype.isSuspended = function() {
        return this._suspended;
    };

    InfiniteScroller.prototype.getScrollTop = function() { return this.$scrollElement.scrollTop(); };
    InfiniteScroller.prototype.setScrollTop = function(scrollTop) { this.$scrollElement.scrollTop(scrollTop); };

    InfiniteScroller.prototype.getPane = function() {
        return this.$scrollElement;
    };
    InfiniteScroller.prototype.getPaneHeight = function() {
        return this.$scrollElement[0].clientHeight;
    };
    InfiniteScroller.prototype.getContentHeight = function() {
        return this.$scrollElement[0].scrollHeight;
    };

    InfiniteScroller.prototype.addScrollListener = function(func) {
        this.$scrollElement.bind('scroll.infinite-scroller', this.scrollDelay ? $.debounce(this.scrollDelay, func) : func);
    };
    InfiniteScroller.prototype.clearScrollListeners = function() {
        this.$scrollElement.unbind('scroll.infinite-scroller');
    };

    InfiniteScroller.prototype.loadIfRequired = function() {

        // if the dev doesn't want us loading, don't load.
        if (this.isSuspended() || !this.options.loadAutomatically) {
            return;
        }

        // if the container is hidden, don't try anything
        if (!$.isWindow(this.getPane()[0]) && this.getPane().is(":hidden")) {
            return;
        }

        var scrollTop = this.getScrollTop(),
            scrollPaneHeight = this.getPaneHeight(),
            contentHeight = this.getContentHeight(),
            scrollBottom = scrollPaneHeight + scrollTop;

        var itemsBefore = this.loadedRange.itemsBefore(),
            itemsLoaded = this.loadedRange.itemsLoaded(),
            emptyAreaRatio = (itemsBefore / (itemsBefore + itemsLoaded));

        if (scrollTop  < (emptyAreaRatio * contentHeight) + this.options.bufferPixels) {
            var pageBefore = this.loadedRange.pageBefore(this.options.pageSize);
            if (pageBefore) {
                return this.load(pageBefore, true);
            }
        }

        // In Chrome on Windows at some font sizes (Ctrl +), the scrollPaneHeight is rounded down, but contentHeight is
        // rounded up (I think). This means there is a 1px difference between them and the event won't fire.
        var chromeWindowsFontChangeBuffer = 1;

        if (scrollBottom + chromeWindowsFontChangeBuffer >= contentHeight - this.options.bufferPixels) {
            var pageAfter = this.loadedRange.pageAfter(this.options.pageSize);
            if (pageAfter) {
                return this.load(pageAfter, false);
            }
        }
    };

    function loadInternal(self, pageRequest, shouldLoadAbove) {

        if (self.currentXHR) {
            return $.Deferred().reject();
        }

        self.currentXHR = self.requestData(pageRequest);

        return self.currentXHR.complete(function () {
                self.currentXHR = null;
            })
            .done(function(data) {
                self.onDataLoaded(pageRequest, data, shouldLoadAbove);
            })
            .fail(function() {
                if (self.options.suspendOnFailure) {
                    self.suspend();
                }
            });
    }

    InfiniteScroller.prototype.load = function(pageRequest, shouldLoadAbove) {
        var self = this;
        return loadInternal(this, pageRequest, shouldLoadAbove).fail(function() {
            if (!self._cancelling) {
                self.handleErrors.apply(self, arguments);
            }
        });
    };

    InfiniteScroller.prototype.onDataLoaded = function(pageRequest, data, isLoadedAbove) {
        var firstLoad = this.loadedRange.isEmpty(),
            attachmentMethod = this.loadedRange.isEmpty() ?
                                    'html' :
                               isLoadedAbove ?
                                    'prepend' :
                                    'append',
            isPrepend = attachmentMethod === 'prepend';

        this.loadedRange.add(pageRequest, data);

        var oldHeight,
            oldScrollTop;
        if (isPrepend || isIE) { // values for calculating offset
            oldScrollTop = this.getScrollTop();
            oldHeight = this.getContentHeight();
        }

        this.attachNewContent(data, attachmentMethod);

        // scroll to where the user was before we added new data.  IE reverts to the initial position (top or line
        // specified in hash) when you append content, so we need to always rescroll in IE.
        if (isPrepend || isIE) {
            var heightAddedAbove = isPrepend ? this.getContentHeight() - oldHeight : 0;
            this.setScrollTop(oldScrollTop + heightAddedAbove);
        }

        if (firstLoad) {
            this.onFirstDataLoaded(pageRequest, data);
        }

        this.options.eventBus('aui.infinitescroller.dataLoaded', this, pageRequest, data );

        //retrigger scroll - load more if we're still at the edges.
        this.loadIfRequired();
    };

    InfiniteScroller.prototype.onFirstDataLoaded = function(pageRequest, data) {
        var self = this;
        this.addScrollListener(function() { self.loadIfRequired(); });

        $(window).bind('resize', this._resizeHandler = function() {
            self.loadIfRequired();
        });
    };


    InfiniteScroller.prototype.attachNewContent = function(data, attachmentMethod) {
        console.error('attachNewContent is abstract and must be implemented.');
    };

    InfiniteScroller.prototype.requestData = function(pageRequest) {
        console.error('requestData is abstract and must be implemented.  It must return a promise. It is preferred to return a jqXHR.');
    };

    InfiniteScroller.prototype.cancelRequest = function() {
        if (this.currentXHR) {
            this._cancelling = true;
            if (this.currentXHR.abort) {
                this.currentXHR.abort();

            } else if (this.currentXHR.reject) {
                this.currentXHR.reject();

            } else {
                console.log("Couldn't cancel the current request.");
            }
            this._cancelling = false;
            this.currentXHR = null;
        }
    };

    InfiniteScroller.prototype.handleErrors = function(/* arguments */) {
        console.error("handleErrors is abstract and must be implemented. It is called by your promise's fail handler, except when the request was cancelled by InfiniteScroller.");
    };


    exports.InfiniteScroller = InfiniteScroller;
})(
    AJS,
    {
        error : window.console && console.error ?
                function() {
                    console.error.apply(console, arguments);
                } :
                AJS.log,
        log : AJS.log
    },
    AJS.$,
    AJS.LoadedRange,
    function(eventName, context /*, arguments */) {
        AJS.$(context).trigger(eventName, Array.prototype.slice.call(arguments, 2));
    }
);