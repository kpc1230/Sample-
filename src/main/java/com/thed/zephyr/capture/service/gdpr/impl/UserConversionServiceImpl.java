package com.thed.zephyr.capture.service.gdpr.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.hazelcast.core.HazelcastInstance;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.service.gdpr.UserConversionService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thed.zephyr.capture.util.ApplicationConstants.JIRA_BULK_USER_LIMIT;

/**
 * Created by Masud on 4/25/19.
 */
@Service
public class UserConversionServiceImpl implements UserConversionService {

    @Autowired
    private Logger log;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void pullUserKeyFromSessions(String ctId) {
        log.debug("Start pulling user keys from session --> {}",ctId);
        Set<String> userKeys = new HashSet<>();

        int start = 0;
        int maxResult = 500;
        boolean continueWhile = true;
        do {
            PageRequest pageRequest = CaptureUtil.getPageRequest(start, maxResult);
            Page<Session> sessions = sessionRepository.findByCtId(ctId, pageRequest);

            if(sessions != null){
                sessions.forEach(session -> {
                    //Take care assignee
                    String assignee = session.getAssignee();
                    if(assignee != null) {
                        userKeys.add(assignee);
                    }

                    //Take care participant
                    Collection<Participant> participants = session.getParticipants();
                    if(participants != null && participants.size() > 0){
                        participants.forEach(participant -> {
                            userKeys.add(participant.getUser());
                        });
                    }

                    //Take care creator
                    String creator = session.getCreator();
                    if(creator != null) {
                        userKeys.add(creator);
                    }

                });
            }else {
                continueWhile = false;
            }

            start = start + maxResult;

        }while (continueWhile);

        log.debug("End pulling user keys from session --> {}",ctId);
        if(userKeys != null && userKeys.size() > 0){
            int partStart = 0, partMax = 200, size = userKeys.size();

            do {
                Set<String> userKey200 = userKeys.stream()
                        .skip(partStart)
                        .limit(partMax)
                        .collect(Collectors.toSet());

                pullUserAccountIdFromJira(ctId, userKey200.stream().collect(Collectors.toList()));

                partStart = partStart + partMax;
            }while (partStart <= size);
        }

    }

    @Override
    public void pullUserAccountIdFromJira(String ctId, List<String> userKeys) {
        if(userKeys != null && userKeys.size() > 0) {
            AtlassianHostUser hostUser = (AtlassianHostUser) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String bulk_user_url = acHostModel.getBaseUrl() + "/" + JiraConstants.REST_API_SEARCH_USER_BULK;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(bulk_user_url)
                    .queryParam("startAt", 0)
                    .queryParam("maxResults", JIRA_BULK_USER_LIMIT);
            final MultiValueMap<String, String> userParams = new LinkedMultiValueMap<>();
            userKeys.forEach(user -> {
                userParams.add("key", user);
            });
            builder.queryParams(userParams);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            HttpEntity<JsonNode> response = atlassianHostRestClients
                    .authenticatedAs(hostUser).exchange(
                            builder.toUriString(),
                            HttpMethod.GET,
                            entity,
                            JsonNode.class);
            if(response != null && response.hasBody()){
                //TODO found user list with accountId.
                //put it under hazelcast cache
            }

        }
    }
}
