package com.thed.zephyr.capture.model.util;

import java.util.List;

import com.thed.zephyr.capture.model.Variable;

public class VariableSearchList extends SearchList<Variable>{
	public VariableSearchList(List<Variable> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
