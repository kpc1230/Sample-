package com.thed.zephyr.capture.repositories;

import com.thed.zephyr.capture.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by aliakseimatsarski on 8/27/17.
 */
public interface NoteRepository extends CrudRepository<Note, String> {

    Page<Note> queryByCtIdAndSessionId(String ctId, String sessionId, Pageable pageable);
}
