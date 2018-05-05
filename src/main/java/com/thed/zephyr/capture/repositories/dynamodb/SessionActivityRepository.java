package com.thed.zephyr.capture.repositories.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.thed.zephyr.capture.model.SessionActivity;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
@Repository
public interface SessionActivityRepository extends CrudRepository<SessionActivity, String> {

    List<SessionActivity> findBySessionId(String sessionId);

    List<SessionActivity> findBySessionId(String sessionId, Optional<String> propertyName);

    @Deprecated
    Map<String, Object> findByCtId(String ctid, Map<String, AttributeValue> lastKeyEvaluated);

    @EnableScan
    @EnableScanCount
    Page<SessionActivity> findAll(Pageable pageRequest);


}
