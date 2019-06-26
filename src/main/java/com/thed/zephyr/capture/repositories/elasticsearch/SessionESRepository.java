package com.thed.zephyr.capture.repositories.elasticsearch;

import com.thed.zephyr.capture.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by aliakseimatsarski on 9/7/17.
 */
@Repository
public interface SessionESRepository extends ElasticsearchRepository<Session, String> {
	
	@Query("{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"ctId\":\"?0\"}},{\"match\":{\"issuesRaised.issueId\":?1}}]}}}")
    Page<Session> findByCtIdAndProjectIdAndIssueId(String ctId, Long issueId, Pageable pageable);

    Page<Session> findByCtIdAndProjectIdAndRelatedIssueIds(String ctId, Long projectId, Long relatedIssueId, Pageable pageable);

    Page<Session> findByCtIdAndStatusAndAssignee(String ctId, String status, String assignee,Pageable pageable);

    Page<Session> findByCtIdAndStatusAndCreator(String ctId, String status, String creator,Pageable pageable);

    Page<Session> findByCtIdAndStatusAndParticipantsUser(String ctId, String status, String participants,Pageable pageable);
    
    @Query("{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"ctId\":\"?0\"}},{\"match\":{\"status\":\"?1\"}}, {\"match_phrase\":{\"participants.userAccountId\":\"?2\"}}]}}}")
    Page<Session> findByCtIdAndStatusAndParticipantsUserAccountId(String ctId, String status, String participants,Pageable pageable);

    List<Session> findByCtIdAndStatusAndAssignee(String ctId, String status, String assignee);
    
    @Query("{\"query\":{\"bool\":{\"must\":[{\"match_phrase\":{\"ctId\":\"?0\"}},{\"match\":{\"status\":\"?1\"}}, {\"match_phrase\":{\"assigneeAccountId\":\"?2\"}}]}}}")
    List<Session> findByCtIdAndStatusAndAssigneeAccountId(String ctId, String status, String assigneeAccountId);

    Session findById(String id);

    long countByCtId(String ctId);
    

    Map<String, Object> searchSessions(String ctId, List<Long> projectIds, Optional<String> assignee, Optional<String> assigneeAccountId, Optional<List<String>> status, Optional<String> searchTerm,
                                       Optional<String> sortField, boolean sortAscending, int startAt, int size);
    
    Set<String> fetchAllAssigneesForCtId(String ctId);
    
    void deleteSessionsByCtId(String ctId);
    
    AggregatedPage<Session> fetchPrivateSessionsForUser(String ctId, String user, String userAccountId,List<Long> projectIds);
	
    AggregatedPage<Session> fetchSharedSessionsForUser(String ctId, String user, String userAccountId,List<Long> projectIds);

    Session findByCtIdAndJiraPropIndex(String ctId, String jiraPropIndex);

    List<Session> findByCtIdAndRelatedIssueIds(String ctId, String relatedIssueId);

    List<Session> findByCtIdAndIssuesRaised(String ctId, String issueRaisedId);
}
