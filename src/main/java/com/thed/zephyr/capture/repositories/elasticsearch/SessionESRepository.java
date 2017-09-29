package com.thed.zephyr.capture.repositories.elasticsearch;

import com.thed.zephyr.capture.model.Session;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by aliakseimatsarski on 9/7/17.
 */
@Repository
public interface SessionESRepository extends ElasticsearchRepository<Session, String> {
	
	@Query("{\"query\":{\"bool\":{\"must\":[{\"match\":{\"ctId\":\"?0\"}},{\"match\":{\"issuesRaised.issueId\":?1}}]}}}")
    Page<Session> findByCtIdAndProjectIdAndIssueId(String ctId, Long issueId, Pageable pageable);

    Page<Session> findByCtIdAndProjectIdAndRelatedIssueIds(String ctId, Long projectId, Long relatedIssueId, Pageable pageable);

    Page<Session> findByCtIdAndStatusAndAssignee(String ctId, String status, String assignee,Pageable pageable);

    Page<Session> findByCtIdAndStatusAndCreator(String ctId, String status, String creator,Pageable pageable);

    Page<Session> findByCtIdAndStatusAndParticipants(String ctId, String status, String participants,Pageable pageable);
    
    List<Session> findByCtIdAndStatusAndAssignee(String ctId, String status, String assignee);

    long countByCtId(String ctId);
}
