package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.thed.zephyr.capture.model.jira.Version;
import com.thed.zephyr.capture.service.VersionService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return null;
    }

    @Override
    public List<Version> getVersionsReleased(Long projectId) {
        return null;
    }
}
