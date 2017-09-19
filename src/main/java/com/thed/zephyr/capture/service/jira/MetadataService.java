package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.jira.CaptureProject;

import java.util.Map;

/**
 * Created by Masud on 8/22/17.
 */
public interface MetadataService {

    Map<String, Object> createFieldScreenRenderer(CaptureProject captureProject);
}
