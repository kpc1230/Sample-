package com.thed.zephyr.capture.model.util;

import java.util.List;

import com.thed.zephyr.capture.model.Note;

public class NoteSearchList extends SearchList<Note>{
	public NoteSearchList(List<Note> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
