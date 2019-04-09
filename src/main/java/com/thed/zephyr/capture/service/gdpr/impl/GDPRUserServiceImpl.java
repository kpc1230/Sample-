package com.thed.zephyr.capture.service.gdpr.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.util.concurrent.Promise;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.service.gdpr.GDPRUserService;
import com.thed.zephyr.capture.service.gdpr.model.UserDTO;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Masud on 4/1/19.
 */
@Service
public class GDPRUserServiceImpl implements GDPRUserService {

    @Autowired
    private Logger log;

//    @Autowired
//    private AtlassianHostRestClients atlassianHostRestClients;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private JiraRestClient getJiraRestClient;

    @Autowired
    private JedisPool userRedisPool;


    @Override
    public List<UserDTO> getAndPushUserToMigration(AtlassianHostUser hostUser) {
        List<UserDTO> userDTOS = new ArrayList<>();
        try {
            AcHostModel acHostModel = null;
            Boolean proceedStoring = dynamicProperty.getBoolProp(ApplicationConstants.STORE_ACCOUNT_INFO, false).getValue();
            if(!proceedStoring) {
                log.info("Skipped storing account info since {} is not define/active", ApplicationConstants.STORE_ACCOUNT_INFO);
            }else{
                ObjectMapper mapper = new ObjectMapper();
                try {
                    acHostModel = acHostModelRepository.findByClientKey(hostUser.getHost().getClientKey()).get(0);
                    if (acHostModel.getCapturedAccountId() != null && acHostModel.getCapturedAccountId().equals(true)) {
                        log.info("Skipped storing account info since {} is already stored", acHostModel.getClientKey());
                    }else{
                        int start = 0;
                        int maxResult = 500;
                        boolean continueWhile = true;
                        do {
                            JsonNode jsonNode = null;
                            try {
                                String userPath = JiraConstants.REST_API_SEARCH_USER.replace("{start}", String.valueOf(start))
                                        .replace("{limit}", String.valueOf(maxResult));

                                //TODO test did not worked but we could try later.
//                              jsonNode = atlassianHostRestClients.authenticatedAs(hostUser)
//                              .getForObject(userPath, JsonNode.class);

                                //TODO test worked using jrjc 4.0.1
                                Promise<User> userPromise = getJiraRestClient.getUserClient()
                                        .getUser(new URI(hostUser.getHost().getBaseUrl() + userPath));
                                Object object = userPromise.claim();
                                jsonNode = mapper.readValue(String.valueOf(object), JsonNode.class);
                                if (jsonNode != null) {
                                    log.debug("result {}", jsonNode);
                                } else {
                                    log.error("Error getting account id for tenantKey {}", hostUser.getHost().getClientKey());
                                }
                                boolean processed = false;
                                if (jsonNode != null) {
                                    if (jsonNode.isArray()) {
                                        for (JsonNode jsonNode1 : jsonNode) {
                                            processed = true;
                                            String userKey = jsonNode1.get("key").asText();
                                            //No need to store addon_users
                                            if (!userKey.startsWith("addon_")) {
                                                userDTOS.add(new UserDTO(userKey, jsonNode1.get("name").asText(), jsonNode1.get("accountId").asText()
                                                ));
                                            }
                                        }
                                    }
                                }
                                if (processed) {
                                    start = start + maxResult;
                                } else {
                                    continueWhile = false;
                                }
                            } catch (Exception exp) {
                                continueWhile = false;
                                exp.printStackTrace();
                                log.error("Error while getting all users for the client : " + hostUser.getHost().getBaseUrl(), exp);
                            }
                        }
                        while (continueWhile);

                        if (userDTOS != null && userDTOS.size() > 0) {
                            acHostModel.setCapturedAccountId(true);
                            acHostModelRepository.save(acHostModel);
                        }
                    }
                } catch (Exception ex) {
                    log.error("error during retrive user {}", ex.getMessage());
                }
                if (userDTOS != null && userDTOS.size() > 0) {
                    processToPushMigration(userDTOS, hostUser.getHost().getClientKey(), acHostModel.getCtId());
                }
            }
        }catch (Exception e){
            log.error("Error during --> getAndPushUserToMigration {}", e.getMessage());
        }
        return userDTOS;
    }


    /**
     * Pass account info to auditing server
     * @param userList
     * @param tenantId
     */
    @Override
    public void processToPushMigration(List<UserDTO> userList, String tenantId, String ctId){

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(ApplicationConstants.TENANTID, tenantId);
        requestMap.put(ApplicationConstants.TENANT_ID_FIELD, ctId);
        requestMap.put(ApplicationConstants.PROJECT, ApplicationConstants.PROJECT_TYPE);
        requestMap.put(ApplicationConstants.USERS, userList);
        ObjectMapper objectMapper = new ObjectMapper();
        try (Jedis jedis = userRedisPool.getResource()){
            ObjectMapper om = new ObjectMapper();
            String strUserQueue = null;
            try {
                strUserQueue = om.writeValueAsString(requestMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            jedis.lpush(ApplicationConstants.USERDETAILS_EVENTS_QUEUE, strUserQueue);
            log.debug("Push user details into Redis queue for: {} , {}", tenantId, objectMapper.convertValue(requestMap, JsonNode.class));
        }catch (Exception ex){
            log.error("Error during push user into auditing {}", ex.getMessage());
        }


//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(ApplicationConstants.TENANTID, tenantId);
//        requestMap.put(ApplicationConstants.PROJECT, ApplicationConstants.PROJECT_TYPE);
//        requestMap.put(ApplicationConstants.USERS, userList);
//        RestTemplate restTemplate = new RestTemplate();
//        String auditingServerUrl = dynamicProperty.getStringProp(ApplicationConstants.AUDITING_SERVER_URL,"").getValue();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        ObjectMapper mapper = new ObjectMapper();
//        HttpEntity<String> httpEntity = new HttpEntity<String>(mapper.convertValue(requestMap,JsonNode.class).toString(), httpHeaders);
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(auditingServerUrl, httpEntity, String.class);
//            if(response.getStatusCodeValue()==200){
//                log.debug("Response from auditing server: {}", response.getBody());
//            }else{
//                log.error("Response from auditing server: {}", response.getBody());
//            }
//        }catch (Exception ex){
//            log.error("Response from auditing server: {}", ex.getMessage());
//        }

    }

}
