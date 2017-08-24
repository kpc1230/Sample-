package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.VersionService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class VersionServiceImpl implements VersionService {

    @Autowired
    private Logger log;

    @Autowired
    private ProjectService projectService;

    @Override
    public List<Version> getVersionsUnreleased(Long projectId) {
        return getVersionsByRelease(projectId,false);
    }

    @Override
    public List<Version> getVersionsReleased(Long projectId) {
        return getVersionsByRelease(projectId,true);
    }

    private List<Version> getVersionsByRelease(Long projectId, Boolean released) {
      List<Version> versions = new ArrayList<>();

        projectService.getProjectObj(projectId).getVersions()
                .forEach(version -> {
                    log.info("VERSION: {}",version.getName());
                    if(released.equals(version.isReleased())) {
                        versions.add(version);
                    }
                });


        return versions;
    }
}
