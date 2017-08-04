package com.atlassian.excalibur.view;

/**
 * UI object for doing request-based pagination of items in a list
 *
 * @since v1.4
 */
public class PagerTool {
    /**
     * Default maximum number of items shown per page
     */
    private static final int DEFAULT_ITEMS_PER_PAGE = 20;
    /**
     * Current page number - zero indexed
     * Why an Integer? Because Velocity doesn't like Longs.
     */
    private final Integer pageNumber;
    /**
     * Number of items in a page
     */
    private final int itemsPerPage;
    /**
     * pageNumber of the previous page - null if no previous page
     */
    private final Integer previous;
    /**
     * pageNumber of the next page - null if no next page
     */
    private final Integer next;

    public PagerTool(Integer pageNumber, boolean hasNext) {
        this(pageNumber, hasNext, DEFAULT_ITEMS_PER_PAGE);
    }

    public PagerTool(Integer pageNumber, boolean hasNext, int itemsPerPage) {
        this.pageNumber = Math.max(0, pageNumber);
        this.itemsPerPage = itemsPerPage;

        if (pageNumber > 0) {
            this.previous = pageNumber - 1;
        } else {
            this.previous = null;
        }

        if (hasNext) {
            this.next = pageNumber + 1;
        } else {
            this.next = null;
        }
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getDisplayPageNumber() {
        return pageNumber + 1;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public Integer getPrevious() {
        return previous;
    }

    public Integer getNext() {
        return next;
    }
}
