package com.thed.zephyr.capture.service.gdpr;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;

public interface MigrateService {
    void migrateData(AtlassianHostUser hostUser,AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException;
}
