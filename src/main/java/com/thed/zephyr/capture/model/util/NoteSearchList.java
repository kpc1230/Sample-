package com.thed.zephyr.capture.model.util;

import java.util.List;

import com.thed.zephyr.capture.model.NoteRequest;

public class NoteSearchList extends SearchList<NoteRequest>{

    public NoteSearchList() {
    }

    public NoteSearchList(List<NoteRequest> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
