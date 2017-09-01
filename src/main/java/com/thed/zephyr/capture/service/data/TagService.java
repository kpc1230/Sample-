package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.Tag;

import java.util.List;
import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/31/17.
 */
public interface TagService {

    Set<String> parseTags(String noteData);

    List<Tag> saveTags(Note note);

    Tag saveTag(Tag rowTag);

    void deleteTags(String noteId);

}
