package com.thed.zephyr.capture.model.util;

import java.util.List;

import com.thed.zephyr.capture.model.TemplateRequest;

public class TemplateSearchList extends SearchList<TemplateRequest>{
	public TemplateSearchList(List<TemplateRequest> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
