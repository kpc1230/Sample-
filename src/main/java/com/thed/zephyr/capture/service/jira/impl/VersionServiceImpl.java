package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.thed.zephyr.capture.model.jira.Version;
import com.thed.zephyr.capture.service.jira.VersionService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class VersionServiceImpl implements VersionService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Override
    public List<Version> getVersionsUnreleased(Long projectId) {
        return getVersionsByRelease(projectId,false);
    }

    @Override
    public List<Version> getVersionsReleased(Long projectId) {
        return getVersionsByRelease(projectId,true);
    }

    private List<Version> getVersionsByRelease(Long projectId, Boolean released) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        List<Version> versions = new ArrayList<>();
        String uri = host.getHost().getBaseUrl()+ JiraConstants.REST_API_PROJECT+"/"+projectId
                +"/"+ ApplicationConstants.VERSIONS;
        try {
            Version[] listVersion = restTemplate.getForObject(uri, Version[].class);
            Arrays.asList(listVersion).forEach((version -> {
                if(version.getReleased().equals(released)){
                    versions.add(version);
                }
            }));
        } catch (Exception exception) {
            log.error("Error during getting list of {} version from jira.",released,exception);
        }
        return versions;
    }
}
