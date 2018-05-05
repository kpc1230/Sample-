package com.thed.zephyr.capture.service.ac;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by aliakseimatsarski on 8/11/17.
 */
public interface DynamoDBAcHostRepository  extends AtlassianHostRepository {

    AcHostModel findByCtId(String ctId);
}
