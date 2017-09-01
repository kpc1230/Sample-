package com.thed.zephyr.capture.repositories.elasticsearch;


import com.thed.zephyr.capture.model.Tag;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by aliakseimatsarski on 8/29/17.
 */
@Repository
public interface TagRepository extends ElasticsearchRepository<Tag, String> {

    List<Tag> findByCtIdAndSessionId(String ctId, String sessionId);

    List<Tag> findByCtIdAndName(String ctId, String name);

    List<Tag> findByCtIdAndNoteIds(String ctId, String noteId);

    Tag findByCtIdAndProjectIdAndSessionId(String ctId, Long projectId, String sessionId);

    Tag findByCtIdAndNameAndProjectIdAndSessionId(String ctId, String name, Long projectId, String sessionId);
}
