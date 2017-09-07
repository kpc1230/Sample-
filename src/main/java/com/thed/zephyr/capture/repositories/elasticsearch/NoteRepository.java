package com.thed.zephyr.capture.repositories.elasticsearch;

import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteSessionActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Created by aliakseimatsarski on 9/6/17.
 */
@Repository
public interface NoteRepository extends ElasticsearchRepository<Note, String> {

    Note findByCtIdAndNoteSessionActivityId(String ctId, String noteSessionActivityId);

    Page<Note> findByCtIdAndProjectId(String ctId, String projectId, Pageable pageable);

    Page<Note> findByCtIdAndProjectIdAndResolutionStateAndTags(String ctId, String projectId, NoteSessionActivity.Resolution resolution, Set<String> tags, Pageable pageable);

    Page<Note> findByCtIdAndProjectIdAndTags(String ctId, String projectId, Set<String> tags, Pageable pageable);

    Page<Note> findByCtIdAndProjectIdAndResolutionState(String ctId, String projectId, NoteSessionActivity.Resolution resolution, Pageable pageable);

}
