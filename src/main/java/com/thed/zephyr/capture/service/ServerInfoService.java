package com.thed.zephyr.capture.service;

import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.serverInfo.AddonServerInfo;

import java.util.Optional;

/**
 * Created by Masud on 9/15/17.
 */
public interface ServerInfoService {

    AddonServerInfo getAddonServerInfo();
    Optional<ServerInfo> getJiraServerInfo(AcHostModel acHostModel);
    Optional<Integer> getProjectsCount(AcHostModel acHostModel);
    Optional<Integer> getIssuesCount(AcHostModel acHostModel);
    Optional<Integer> getAttachmentsCount(AcHostModel acHostModel);
    Optional<Integer> getSessionsCount(AcHostModel acHostModel);

}
