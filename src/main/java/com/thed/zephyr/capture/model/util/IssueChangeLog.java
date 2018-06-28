package com.thed.zephyr.capture.model.util;

import java.util.List;

public class IssueChangeLog {

    private Long id;

    private List<IssueChangeLogItem> items;

    public IssueChangeLog() {
    }

    public IssueChangeLog(Long id, List<IssueChangeLogItem> items) {
        this.id = id;
        this.items = items;
    }

    public Long getId() {
        return id;
    }

    public List<IssueChangeLogItem> getItems() {
        return items;
    }
}
