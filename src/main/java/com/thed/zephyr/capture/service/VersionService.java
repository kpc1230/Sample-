package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.Version;

import java.util.List;

/**
 * Created by Masud on 8/13/17.
 */
public interface VersionService {
    List<Version> getVersionsUnreleased(Long projectId);
    List<Version> getVersionsReleased(Long projectId);
}
