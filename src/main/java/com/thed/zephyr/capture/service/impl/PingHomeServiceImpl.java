package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.nimbusds.jwt.JWTClaimsSet;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.serverInfo.AddonServerInfo;
import com.thed.zephyr.capture.service.PingHomeService;
import com.thed.zephyr.capture.service.ServerInfoService;
import com.thed.zephyr.capture.service.data.LicenseService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.PlainRestTemplate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Masud on 9/15/17.
 */

@Service
public class PingHomeServiceImpl implements PingHomeService{

    @Autowired
    private Logger log;

    @Autowired
    private ServerInfoService serverInfoService;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private PlainRestTemplate restTemplate;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Override
    public void dialHome(AcHostModel acHostModel) {
        try {
            Map<String, String> params = Maps.newHashMap();
            params.put("baseUrl", acHostModel.getBaseUrl());
            params.put("tenantKey", acHostModel.getClientKey());
            Optional<ServerInfo> serverInfoOption = serverInfoService.getJiraServerInfo(acHostModel);
            params.put("jiraVersion", serverInfoOption.get().getVersion() + "-" + serverInfoOption.get().getBuildNumber());
            AddonServerInfo addonServerInfo = serverInfoService.getAddonServerInfo();
            params.put("buildNumber", addonServerInfo.getVersion());
            params.putAll(populateLicParams(acHostModel));
            StringBuffer checkSum = new StringBuffer();
            //Projects
            Optional<Integer> projectsOpt = serverInfoService.getProjectsCount(acHostModel);
            if (projectsOpt.isPresent())
                checkSum.append(StringUtils.leftPad(String.valueOf(projectsOpt.get()), 4, '0'));


            //Executions
            log.debug("finding all Sessions for tenantId : " + acHostModel.getClientKey());
            Optional<Integer> sessionsOpt = serverInfoService.getSessionsCount(acHostModel);
            if (sessionsOpt.isPresent())
                checkSum.append(StringUtils.leftPad(String.valueOf(sessionsOpt.get()), 6, '0'));
            else
                checkSum.append(StringUtils.leftPad("", 6, '?'));

            params.put("chksum", checkSum.toString());
            JsonNode jsonNode = new ObjectMapper().convertValue(params, JsonNode.class);;
            log.debug("Ping String - " + jsonNode);

            // sending the ping
            String url = dynamicProperty.getStringProp(ApplicationConstants.PING_HOME_URL, ApplicationConstants.PING_HOME_VERSION_CHECK_URL).getValue();

            HttpHeaders requestHeaders = new HttpHeaders();
            HttpEntity<?> httpEntity = new HttpEntity<Object>(jsonNode.toString(), requestHeaders);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JsonNode.class);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                int code = response.getStatusCodeValue();
                if (code == HttpURLConnection.HTTP_OK) {
                    log.debug("version check completed with HTTP Response status:\n" + response.getStatusCodeValue() + "\n" + response.getBody());
                } else if (code == HttpURLConnection.HTTP_UNAUTHORIZED || code == HttpURLConnection.HTTP_FORBIDDEN) {
                    log.error("Unable to perform version check. Response from Server:\n" + response.getStatusCodeValue() + "\n" + response.getBody());
                } else if (code == HttpURLConnection.HTTP_PROXY_AUTH) {
                    log.error("Unable to perform version check. Proxy authentication required. Response from Server: \n" + response.getStatusCodeValue() + "\n" + response.getBody());
                } else {
                    log.error("Unable to perform version check. Response from Server:\n" + response.getStatusCodeValue() + "\n" + response.getBody());
            }
        }else{
                log.error("Error pinging home: ", response.getBody());
            }

        } catch (RuntimeException e) {
            log.error("Error parsing Dial home ping parameters - ", e);
        }
    }

    @Override
    public void runPing() {
        acHostModelRepository.findAll()
                .forEach(acHostModel -> {
                    JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModel, Optional.ofNullable(null)), new JWTClaimsSet());
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                    dialHome(acHostModel);
                });
    }

    private Map<String, String> populateLicParams(AcHostModel acHostModel) {
        Map<String, String> licParams = Maps.newHashMap();
        try {
            Optional<AddonInfo> licenseInfoOption = licenseService.getAddonInfo(acHostModel);
            if (licenseInfoOption.isPresent()) {
                AddonInfo.License licenseInfo = licenseInfoOption.get().getLicense();
                if (null != licenseInfo) {
                    licParams.put("active", String.valueOf(licenseInfo.isActive()));
                    licParams.put("licType", licenseInfo.getType());
                    licParams.put("licenseId", licenseInfo.getSupportEntitlementNumber());
                }else{
                    licParams.put("active", AcHostModel.TenantStatus.LIC_EXPIRED.name());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch license status for ping, " + acHostModel.getBaseUrl(), e);
            if(!licParams.containsKey("active"))
                licParams.put("active", AcHostModel.TenantStatus.HOST_UNREACHABLE.name());
        }
        return licParams;
    }


}
