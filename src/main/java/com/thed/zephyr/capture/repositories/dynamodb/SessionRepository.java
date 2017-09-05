package com.thed.zephyr.capture.repositories.dynamodb;

import com.thed.zephyr.capture.model.Session;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
@Repository
public interface SessionRepository extends CrudRepository<Session, String> {

	Page<Session> queryByCtIdAndProjectId(String ctId, Long projectId, Pageable pageable);
	
	List<Session> searchSessions(String ctId, Optional<Long> projectId, Optional<String> assignee, Optional<String> status, Optional<String> searchTerm);
}
