package com.thed.zephyr.capture.model.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
abstract public class SearchList<T> {

    private List<T> content;

    private int offset;

    private int limit;

    private long total;

    public SearchList() {
        content = new ArrayList<>();
        total = 0;
    }

    public SearchList(List<T> content, int offset, int limit, long total) {
        this.content = content;
        this.offset = offset;
        this.limit = limit;
        this.total = total;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
