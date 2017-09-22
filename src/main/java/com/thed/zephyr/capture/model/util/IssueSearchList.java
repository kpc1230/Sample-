package com.thed.zephyr.capture.model.util;

import java.util.List;

import com.thed.zephyr.capture.model.view.IssueSearchDto;
/**
 * @author manjunath
 *
 */
public class IssueSearchList extends SearchList<IssueSearchDto> {
	 public IssueSearchList() {
        super();
    }

    public IssueSearchList(List<IssueSearchDto> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
