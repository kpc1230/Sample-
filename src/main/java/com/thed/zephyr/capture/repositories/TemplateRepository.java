package com.thed.zephyr.capture.repositories;

import com.thed.zephyr.capture.model.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by aliakseimatsarski on 8/20/17.
 */
@Repository
public interface TemplateRepository extends CrudRepository<Template, String> {
}
