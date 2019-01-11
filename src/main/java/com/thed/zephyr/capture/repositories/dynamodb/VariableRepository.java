package com.thed.zephyr.capture.repositories.dynamodb;

import com.thed.zephyr.capture.model.Variable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by aliakseimatsarski on 9/2/17.
 */
public interface VariableRepository extends CrudRepository<Variable, String> {

    Page<Variable> findByCtIdAndOwnerName(String ctId, String ownerName, Pageable pageable);
    
    Page<Variable> findByCtIdAndOwnerAccountId(String ctId, String ownerAccountId, Pageable pageable);
}
