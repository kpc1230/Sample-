(function(exports) {

    /**
     * A widget for tracking which pages of items have been loaded.
     *
     * For use with AJS.InfiniteScroller
     * 
     * This implementation supports requests for data (pageRequests) that look like 
     * {
     *     start : Number (the index of the first item to retrieve)
     *     limit : Number (the maximum number of items to retrieve after the first)
     *     before : Boolean (whether we're requesting data to come before the currently loaded data)
     *     after : Boolean (whether we're requesting data to come after the currently loaded data)
     * }
     * And expects data to be returned that looks like
     * {
     *     size : Number (the number of items actually returned)
     *     isLastPage : (false if there are more items to be retrieved)
     * }
     * 
     * If you'd like to make InfiniteScroller work with different shapes, you can write your own implementation using
     * this one as your interface.
     */
    function LoadedRange(pageSize, capacity) {
        //assumption: only a contiguous range of lines will ever be loaded.
        this.start = undefined;
        this.end = undefined;
        this._reachedStart = false;
        this._reachedEnd = false;
        this._reachedCapacity = false;
        this._capacity = capacity || Infinity;
        this._pageSize = pageSize;
    }


    // --- private methods ---

    LoadedRange.prototype._isBeforeStart = function(item) {
        return item < this.start;
    };

    LoadedRange.prototype._isAfterEnd = function(item) {
        return item > this.end;
    };



    // --- public methods ---

    /**
     * @return {Boolean} true if no items have been loaded
     */
    LoadedRange.prototype.isEmpty = function() {
        return this.start === undefined;
    };

    /**
     * @param item the item to check the status of
     * @return {Boolean} true if the item is included in this LoadedRange.
     */
    LoadedRange.prototype.isLoaded = function(item) {
        return !(this.isEmpty() || this._isBeforeStart(item) || this._isAfterEnd(item));
    };

    /**
     * @return whether the beginning of the range of items has been loaded.
     */
    LoadedRange.prototype.reachedStart = function() { return this._reachedStart; };
    /**
     * @return whether the end of the range of items has been loaded.
     */
    LoadedRange.prototype.reachedEnd = function() { return this._reachedEnd; };

    /**
     * @return whether the maximum number of items to load has been reached.
     */
    LoadedRange.prototype.reachedCapacity = function() { return this._reachedCapacity; };

    /**
     * @return the number of items preceding this range.
     */
    LoadedRange.prototype.itemsBefore = function() {
        return this.start || 0;
    };

    /**
     * @return the number of items loaded
     */
    LoadedRange.prototype.itemsLoaded = function() {
        return (this.end - this.start) || 0;
    };
    
    /**
     * @param item an identifier for the item you want to request
     * @return a pageRequest for the page that includes the requested item
     */
    LoadedRange.prototype.pageFor = function(item) {

        var start = item ?
            Math.floor(item / this._pageSize) * this._pageSize :
            0;

        return {
            after : this._isBeforeStart(item),
            before: this._isAfterEnd(item),
            start : start,
            limit : this._pageSize
        };
    };

    /**
     * @return a pageRequest for the page immediately following this loaded range.
     */
    LoadedRange.prototype.pageBefore = function() {
        if (this.reachedStart()) return null;

        var start = Math.max(0, this.start - this._pageSize);
        return {
            after : false,
            before: true,
            start : start,
            limit : this.start - start
        };
    };

    /**
     * @return a pageRequest for the page immediately preceding this loaded range.
     */
    LoadedRange.prototype.pageAfter = function() {
        if (this.reachedEnd() || this.reachedCapacity()) return null;

        return {
            after : true,
            before: false,
            start : this.end,
            limit : this._pageSize
        };
    };

    /**
     * Register a range of data as loaded.
     * @param pageRequest the pageRequest used to retrieve this data
     * @param data the data retrieved
     * @return this for chaining
     */
    LoadedRange.prototype.add = function(pageRequest, data) {
        var start = pageRequest.start, size = data.size, isLastPage = data.isLastPage;

        var isEmpty = this.isEmpty();
        if (isEmpty || this._isBeforeStart(start)) {
            this.start = start;
        }
        if (isEmpty || this._isAfterEnd(start + size)) {
            this.end = start + size;
        }

        this._reachedStart = this._reachedStart || start <= 0;
        this._reachedEnd = this._reachedEnd || isLastPage;
        if (this.end >= this._capacity) {
            this._reachedCapacity = true;
        }

        return this;
    };

    AJS.LoadedRange = LoadedRange;
})(AJS);
