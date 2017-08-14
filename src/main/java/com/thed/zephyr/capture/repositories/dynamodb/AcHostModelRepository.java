package com.thed.zephyr.capture.repositories.dynamodb;

import com.thed.zephyr.capture.model.AcHostModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by aliakseimatsarski on 8/13/17.
 */
@Repository
public interface AcHostModelRepository extends CrudRepository<AcHostModel, String> {
    public List<AcHostModel> findByClientKey(String clientKey);
}
