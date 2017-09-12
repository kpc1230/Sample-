package com.thed.zephyr.capture.repositories.elasticsearch;

import com.thed.zephyr.capture.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by aliakseimatsarski on 9/7/17.
 */
@Repository
public interface SessionESRepository extends ElasticsearchRepository<Session, String> {

    Page<Session> findByCtIdAndProjectIdAndIssueRaisedIds(String ctId, Long projectId, Long raisedIssueId, Pageable pageable);

    Page<Session> findByCtIdAndProjectIdAndRelatedIssueIds(String ctId, Long projectId, Long relatedIssueId, Pageable pageable);

    Session findByCtIdAndStatusAndAssignee(String ctId, String status, String assignee);

    Session findByCtIdAndStatusAndCreator(String ctId, String status, String creator);

    Session findByCtIdAndStatusAndParticipants(String ctId, String status, String participants);
}
