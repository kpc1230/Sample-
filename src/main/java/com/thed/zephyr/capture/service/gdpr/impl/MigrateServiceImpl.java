package com.thed.zephyr.capture.service.gdpr.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.util.IOUtils;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.util.concurrent.Promise;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.db.DynamoDBTableNameResolver;
import com.thed.zephyr.capture.service.gdpr.MigrateService;
import com.thed.zephyr.capture.service.gdpr.UserConversionService;
import com.thed.zephyr.capture.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;


@Service
public class MigrateServiceImpl implements MigrateService {
    @Autowired
    private Logger log;

    @Autowired
    private JobProgressService jobProgressService;

    @Autowired
    private LockService lockService;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Autowired
    private DynamoDBTableNameResolver dynamoDBTableNameResolver;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Autowired
    private JiraRestClient getJiraRestClient;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private UserConversionService userConversionService;

    @Autowired
    private ITenantAwareCache tenantAwareCache;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private String KEY_KEY = "key";
    private String KEY_USERNAME = "username";

    @Override
    public void migrateData(AtlassianHostUser hostUser, AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        jobProgressService.createJobProgress(acHostModel, ApplicationConstants.MIGRATE_DATA, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        try {
            if (!lockService.tryLock(acHostModel.getClientKey(), ApplicationConstants.MIGRATE_DATA, 5)) {
                log.warn("Data migration is already progress :{}", acHostModel.getBaseUrl());
                jobProgressService.setErrorMessage(acHostModel, jobProgressId, "Data migration is already progress for Base url : " + acHostModel.getBaseUrl());
                return;
            }
            if (isTenantIsOKToMigrate(acHostModel)) {
                log.debug("Data migration begin: {}",acHostModel.getBaseUrl());
                Map<String, String> accountIdMap = getUserMap(hostUser, acHostModel);
                log.info("Final User map Size used for migration  " + (accountIdMap != null ? accountIdMap.size() : 0));
                if (accountIdMap != null && accountIdMap.size() > 0) {
                    log.info("Working on " + acHostModel.getBaseUrl());
                    updateSessionTable(dynamoDBMapper, acHostModel.getCtId(), accountIdMap, acHostModel, jobProgressId);
                    updateVariableTable(dynamoDBMapper, acHostModel.getCtId(), accountIdMap, acHostModel, jobProgressId);
                    updateTemplateTable(dynamoDBMapper, acHostModel.getCtId(), accountIdMap, acHostModel, jobProgressId);

                    acHostModel.setCreatedByAccountId(getAccountIdFromMap(accountIdMap, acHostModel.getCreatedBy()));
                    acHostModel.setLastModifiedByAccountId(getAccountIdFromMap(accountIdMap, acHostModel.getLastModifiedBy()));

                    acHostModel.setCreatedBy(null);
                    acHostModel.setLastModifiedBy(null);

                    acHostModel.setMigrated(AcHostModel.GDPRMigrationStatus.GDPR);
                    acHostModelRepository.save(acHostModel);

                    //Update tenant cache since its using Spring Security Context to populate tenant
                    CaptureUtil.updateTenantCache(acHostModel, hazelcastInstance);

                    //clear cache invoke
                    log.info("Start of clearTenantCache() : {} ", acHostModel.getBaseUrl());
                    tenantAwareCache.clearTenantCache(acHostModel);
                    log.info("Start of clearTenantCache() : {} ", acHostModel.getBaseUrl());
                    log.info("Start of reindex() : {} ", acHostModel.getBaseUrl());
                    String jobProgressId2 = new UniqueIdGenerator().getStringId();
                    sessionService.reindexSessionDataIntoES(acHostModel, jobProgressId2, acHostModel.getCtId());
                    log.info("End of reindex() : {} ", acHostModel.getBaseUrl());

                    log.info("Done: {}",acHostModel.getBaseUrl());

                  /*  List<AcHostModel> acHostModels = acHostModelRepository.findByBaseUrl(acHostModel.getBaseUrl());
                    AcHostModel acHostModelDB = null;
                    if (acHostModels.size() > 0) {
                        acHostModelDB = acHostModels.get(0);
                        JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModelDB, Optional.ofNullable(null)), new JWTClaimsSet.Builder().build());
                        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                    }*/
                    //
                    jobProgressService.setMessage(acHostModel, jobProgressId, "Data migration Completed");
                    jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_COMPLETED, jobProgressId);

                } else {
                    log.info("Tenant not found any users in three ways ");
                }

            } else {
                log.info("Tenant is not eligible to migrate ");
                jobProgressService.setMessage(acHostModel, jobProgressId, "Tenant is not eligible to migrate ");
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
            }

        } catch (Exception ex) {
            log.error("Error during Data migration for tenant base url:{}", acHostModel.getBaseUrl(), ex);
            try {
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                String errorMessage = "Internal server error while Data migration";
                jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
            } catch (HazelcastInstanceNotDefinedException exception) {
                log.error("Error during Data migration for tenant base url:{}", acHostModel.getBaseUrl(), exception);
            }
        } finally {
            try {
                lockService.deleteLock(acHostModel.getClientKey(), ApplicationConstants.MIGRATE_DATA);
            } catch (HazelcastInstanceNotDefinedException exception) {
                log.error("Error during Data migration  lock for tenant baseurl:{}", acHostModel.getBaseUrl(), exception);
            }
        }
    }

    public void updateSessionTable(DynamoDBMapper mapper, String ctId, Map<String, String> accountIdMap,
                                   AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        int session_total = 0;
        int session_activity_total = 0;
        int session_creator_account_id = 0;
        int session_assignee_account_id = 0;
        int session_total_local = 0;
        int session_creator_account_id_local = 0;
        int session_assignee_account_id_local = 0;
        jobProgressService.setMessage(acHostModel, jobProgressId, "Session and Session Activity tables migration started ");
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_TABLE_NAME);
        DynamoDBMapperConfig tableconfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE).build();
        Session session = new Session();
        session.setCtId(ctId);
        final DynamoDBQueryExpression<Session> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression.setHashKeyValues(session);
        queryExpression.setIndexName("idx_ctId_projectid");
        queryExpression.setConsistentRead(false); // cannot use consistent read on GSI
        final List<Session> results = mapper.query(Session.class, queryExpression, tableconfig);
        if (results != null) {
            jobProgressService.setTotalSteps(acHostModel, jobProgressId, results.size());
        }
        for (Session s : results) {
            boolean needUpdate = true;
            log.info("Starting Session: " + s.getId());
            updateSessionActivityTable(mapper, s.getId(), accountIdMap, acHostModel, jobProgressId);
            String assigneeAccountId = getAccountIdFromMap(accountIdMap, s.getAssignee());
            String creatorAccountId = getAccountIdFromMap(accountIdMap, s.getCreator());
            if (s.getParticipants() != null && s.getParticipants().size() > 0) {
                for (Participant p : s.getParticipants()) {
                    String pAccountId = getAccountIdFromMap(accountIdMap, p.getUser());
                    if (pAccountId != null && p.getUserAccountId() == null) {
                        needUpdate = true;
                        p.setUserAccountId(pAccountId);
                    }
                    p.setUser(null);
                }
            }
            session_total++;
            session_total_local++;
            if (s.getAssigneeAccountId() != null) {
                session_assignee_account_id++;
                session_assignee_account_id_local++;
            } else if (assigneeAccountId != null) {
                s.setAssigneeAccountId(assigneeAccountId);
                session_assignee_account_id++;
                session_assignee_account_id_local++;
                needUpdate = true;
            }
            if (s.getCreatorAccountId() != null) {
                session_creator_account_id++;
                session_creator_account_id_local++;
            } else if (creatorAccountId != null) {
                s.setCreatorAccountId(creatorAccountId);
                session_creator_account_id++;
                session_creator_account_id_local++;
                needUpdate = true;
            }

            s.setCreator(null);
            s.setAssignee(null);

            if (needUpdate) {
                try {
                    mapper.save(s, tableconfig);
                } catch (Exception e) {
                    log.error("Error saving to session table: ", e);
                }
            }
            log.info("Finished Session: " + s.getId());
            jobProgressService.setCompletedSteps(acHostModel, jobProgressId, 1);
        }
        jobProgressService.setMessage(acHostModel, jobProgressId, "Session and Session Activity tables migration completed ");
    }

    public void updateSessionActivityTable(DynamoDBMapper mapper, String sessionId,
                                           Map<String, String> accountIdMap, AcHostModel acHostModel, String jobProgressId) {
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        DynamoDBMapperConfig tableconfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE).build();
        Table sessionActivityTable = new Table(amazonDynamoDB, tableName);
        QuerySpec querySpec = new QuerySpec();
        querySpec.withHashKey(new KeyAttribute(ApplicationConstants.SESSION_ID_FIELD, sessionId));
        Index index = sessionActivityTable.getIndex(ApplicationConstants.GSI_SESSIONID_TIMESTAMP);
        ItemCollection<QueryOutcome> activityItemList = index.query(querySpec);
        IteratorSupport<Item, QueryOutcome> iterator = activityItemList.iterator();
        while (iterator.hasNext()) {
            boolean needToUpdate = false;
            Item item = iterator.next();
            SessionActivity sessionActivity = convertItemToSessionActivity(item);
            if (sessionActivity != null) {
                if (sessionActivity instanceof UserAssignedSessionActivity) {
                    UserAssignedSessionActivity userAssignedSessionActivity = (UserAssignedSessionActivity) sessionActivity;
                    String assigneeAccountId = getAccountIdFromMap(accountIdMap, userAssignedSessionActivity.getAssignee());
                    if (assigneeAccountId != null && userAssignedSessionActivity.getAssigneeAccountId() == null) {
                        userAssignedSessionActivity.setAssigneeAccountId(assigneeAccountId);
                        userAssignedSessionActivity.setAssignee(null);
                        needToUpdate = true;
                        addAccountIdAndSaveSessionActivity(userAssignedSessionActivity, accountIdMap, tableconfig, needToUpdate);
                    }
                } else if (sessionActivity instanceof UserLeftSessionActivity) {
                    UserLeftSessionActivity userLeftSessionActivity = (UserLeftSessionActivity) sessionActivity;
                    if (userLeftSessionActivity.getParticipant() != null) {
                        log.info("Participent user:" + userLeftSessionActivity.getParticipant().getUser());
                        String leftParticipantAccountId = getAccountIdFromMap(accountIdMap, userLeftSessionActivity.getParticipant().getUser());
                        if (leftParticipantAccountId != null && userLeftSessionActivity.getParticipant().getUserAccountId() == null) {
                            userLeftSessionActivity.getParticipant().setUserAccountId(leftParticipantAccountId);
                            userLeftSessionActivity.getParticipant().setUser(null);
                            needToUpdate = true;
                            addAccountIdAndSaveSessionActivity(userLeftSessionActivity, accountIdMap, tableconfig, needToUpdate);
                        }
                    }
                } else if (sessionActivity instanceof UserJoinedSessionActivity) {
                    UserJoinedSessionActivity userJoinedSessionActivity = (UserJoinedSessionActivity) sessionActivity;
                    log.info("Participent user:" + userJoinedSessionActivity.getParticipant().getUser());
                    if (userJoinedSessionActivity.getParticipant() != null) {
                        String joinedParticipantAccountId = getAccountIdFromMap(accountIdMap, userJoinedSessionActivity.getParticipant().getUser());
                        if (joinedParticipantAccountId != null && userJoinedSessionActivity.getParticipant().getUserAccountId() == null) {
                            userJoinedSessionActivity.getParticipant().setUserAccountId(joinedParticipantAccountId);
                            userJoinedSessionActivity.getParticipant().setUser(null);
                            needToUpdate = true;
                            addAccountIdAndSaveSessionActivity(userJoinedSessionActivity, accountIdMap, tableconfig, needToUpdate);
                        }
                    }
                } else {
                    addAccountIdAndSaveSessionActivity(sessionActivity, accountIdMap, tableconfig, needToUpdate);
                }
            }
        }
        log.info("Finished Session activity for session id: " + sessionId);
    }

    public void updateVariableTable(DynamoDBMapper mapper, String ctId, Map<String, String> accountIdMap, AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        jobProgressService.setMessage(acHostModel, jobProgressId, "Variable table migration started ");
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.VARIABLE_TABLE_NAME);
        int variable_owner_accountid_local = 0;
        int variable_owner_total_local = 0;
        int variable_owner_accountid = 0;
        int variable_owner_total = 0;

        DynamoDBMapperConfig tableconfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE).build();
        Variable session = new Variable();
        session.setCtId(ctId);
        final DynamoDBQueryExpression<Variable> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression.setHashKeyValues(session);
        queryExpression.setIndexName("idx_ct_id_owner_name");
        queryExpression.setConsistentRead(false); // cannot use consistent read on GSI
        final List<Variable> results = mapper.query(Variable.class, queryExpression, tableconfig);
        if (results != null) {
            jobProgressService.setTotalSteps(acHostModel, jobProgressId, results.size());
        }
        for (Variable s : results) {
            String creatorAccountId = getAccountIdFromMap(accountIdMap, s.getOwnerName());
            variable_owner_total++;
            variable_owner_total_local++;
            if (s.getOwnerAccountId() != null) {
                variable_owner_accountid++;
                variable_owner_accountid_local++;
            } else if (creatorAccountId != null) {
                s.setOwnerAccountId(creatorAccountId);
            }
            try {
                s.setOwnerName(null);
                mapper.save(s, tableconfig);
                variable_owner_accountid++;
                variable_owner_accountid_local++;
            } catch (Exception e) {
                log.error("Error saving to variable table skipping: " + s.getId(), e);
            }
        }
        log.info("Finish updating varaible table for ctid: " + ctId);
        jobProgressService.setMessage(acHostModel, jobProgressId, "Variable table migration completed ");

    }

    public void updateTemplateTable(DynamoDBMapper mapper, String ctId, Map<String, String> accountIdMap, AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        jobProgressService.setMessage(acHostModel, jobProgressId, "Template table migration started ");
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.TEMPLATE_TABLE_NAME);
        int template_owner_total = 0;
        int template_owner_total_local = 0;
        int template_owner_accountid_local = 0;
        int template_owner_accountid = 0;

        DynamoDBMapperConfig tableconfig = DynamoDBMapperConfig.builder()
                .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE).build();
        Template session = new Template();
        session.setCtId(ctId);
        final DynamoDBQueryExpression<Template> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression.setHashKeyValues(session);
        queryExpression.setIndexName("idx_ctId_projectid");
        queryExpression.setConsistentRead(false); // cannot use consistent read on GSI
        final List<Template> results = mapper.query(Template.class, queryExpression, tableconfig);
        if (results != null) {
            jobProgressService.setTotalSteps(acHostModel, jobProgressId, results.size());
        }
        for (Template s : results) {
            String creatorAccountId = getAccountIdFromMap(accountIdMap, s.getCreatedBy());
            template_owner_total++;
            template_owner_total_local++;
            if (s.getCreatedByAccountId() != null) {
                template_owner_accountid++;
                template_owner_accountid_local++;
            } else if (creatorAccountId != null) {
                s.setCreatedByAccountId(creatorAccountId);

            }
            try {
                s.setCreatedBy(null);
                mapper.save(s, tableconfig);
                template_owner_accountid++;
                template_owner_accountid_local++;
            } catch (Exception e) {
                log.error("Error saving to template table: " + s.getId(), e);
            }
        }
        jobProgressService.setMessage(acHostModel, jobProgressId, "Template table migration completed ");
    }


    private void addAccountIdAndSaveSessionActivity(SessionActivity s, Map<String, String> accountIdMap, DynamoDBMapperConfig tableconfig, boolean needToUpdate) {
        String accountId = getAccountIdFromMap(accountIdMap, s.getUser());
        needToUpdate = true;
        int session_activity_total = 0;
        int session_activity_total_local = 0;
        int session_activity_user_account_id = 0;
        int session_activity_user_account_id_local = 0;

        session_activity_total++;
        session_activity_total_local++;
        if (s.getUserAccountId() != null) {
            session_activity_user_account_id++;
            session_activity_user_account_id_local++;
        } else if (accountId != null) {
            s.setUserAccountId(accountId);
            try {
                needToUpdate = true;
                session_activity_user_account_id++;
                session_activity_user_account_id_local++;
            } catch (Exception e) {
                log.error("Error saving to session activity table: " + s.getId(), e);
            }
        }

        if (needToUpdate) {
            try {
                s.setUser(null);
                dynamoDBMapper.save(s, tableconfig);
            } catch (Exception e) {
                log.error("Error saving to session activity table: " + s.getId(), e);
            }
        }
    }

    private String getAccountIdFromMap(Map<String, String> accountIdMap, String key) {
        String value = accountIdMap.get(key);
        if (value == null || value.length() == 0) {
            log.error("Unable to find user from user :" + key);
            value = getAccountIDFromJiraNewAPI(key);
            if (value == null || value.length() == 0) {
                log.error("Unable to find user from jira 'with new API for user :" + key);
                accountIdMap.put(key, "ACCOUNT_ID_NOT_FOUND");
                value = "ACCOUNT_ID_NOT_FOUND";
            } else {
                accountIdMap.put(key, value);
            }
        }
        return value;
    }

    private String getAccountIDFromJiraNewAPI(String userKey) {
        List<String> list = new ArrayList<>();
        list.add(userKey);
        Map<String, String> resMap = userConversionService.pullUserAccountIdFromJira(list, KEY_KEY);
        if (resMap != null && resMap.size() > 0) {
            return resMap.get(userKey);
        } else {
            resMap = userConversionService.pullUserAccountIdFromJira(list, KEY_USERNAME);
            if (resMap != null && resMap.size() > 0) {
                return resMap.get(userKey);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private SessionActivity convertItemToSessionActivity(Item item) {
        Map<String, AttributeValue> objectMap = new LinkedHashMap<>();
        Map<String, Object> stringObjectMap = item.asMap();
        String clazz = (String) stringObjectMap.get("clazz");
        try {
            Class<?> cls = Class.forName(clazz);
            for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                AttributeValue attributeValue = new AttributeValue();
                if (entry.getValue() instanceof String) {
                    attributeValue.setS(entry.getValue().toString());
                } else if (entry.getValue() instanceof BigDecimal) {
                    attributeValue.setN(entry.getValue().toString());
                } else if (entry.getValue() instanceof Set) {
                    attributeValue.setSS((Set<String>) entry.getValue());
                }
                objectMap.put(entry.getKey(), attributeValue);
            }

            return (SessionActivity) dynamoDBMapper.marshallIntoObject(cls, objectMap);
        } catch (ClassNotFoundException e) {
            log.error("Error during instantiating class:{} from table:{} dynamoDB", clazz, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME, e);
        }
        return null;
    }

    private boolean isTenantIsOKToMigrate(AcHostModel acHostModel) {
        if (acHostModel != null) {
            List<AcHostModel> acHostModels = acHostModelRepository.findByBaseUrl(acHostModel.getBaseUrl());
            AcHostModel acHostModelDB = null;
            if (acHostModels.size() > 0) {
                acHostModelDB = acHostModels.get(0);
            }
            if (acHostModelDB != null) {
                Boolean skip = !AcHostModel.GDPRMigrationStatus.GDPR.equals(acHostModel.getMigrated()) && !AcHostModel.GDPRMigrationStatus.MIGRATED.equals(acHostModel.getMigrated());
                if (acHostModel != null && AcHostModel.TenantStatus.ACTIVE.equals(acHostModel.getStatus()) && skip) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> getUserMap(AtlassianHostUser hostUser, AcHostModel acHostModel) {
        try {
            Map<String, String> userMap = getJiraUsers(hostUser);
            log.info("Users size from Jira : " + (userMap != null ? userMap.size() : 0));
            if (userMap != null && userMap.size() > 0) {
                return userMap;
            } else {
                String auditingServerUrl = dynamicProperty.getStringProp(ApplicationConstants.AUDITING_SERVER_URL, "http://localhost:8000").getValue();
                userMap = getUsersFromAudit(auditingServerUrl, hostUser.getHost().getClientKey());
                log.info("Users size from Audit : " + (userMap != null ? userMap.size() : 0));
                if (userMap != null && userMap.size() > 0) {
                    return userMap;
                } else {
                    userMap = userConversionService.pullUserKeyFromSessions(acHostModel.getCtId());
                    log.info("Users size from new API : " + (userMap != null ? userMap.size() : 0));
                    if (userMap != null && userMap.size() > 0) {
                        return userMap;
                    }
                }
            }
        } catch (Exception exp) {
            log.error("Exception got while getting users from Jira ", exp);
        }
        return new HashMap<>();
    }


    private Map<String, String> getJiraUsers(AtlassianHostUser hostUser) {
        Map<String, String> accountIdMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        int start = 0;
        int maxResult = 500;
        boolean continueWhile = true;
        do {
            JsonNode jsonNode = null;
            try {
                String userPath = JiraConstants.REST_API_SEARCH_USER.replace("{start}", String.valueOf(start))
                        .replace("{limit}", String.valueOf(maxResult));

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
                            String userKey = jsonNode1.has("key") ? jsonNode1.get("key").asText() : "";
                            String userName = jsonNode1.has("name") ? jsonNode1.get("name").asText() : "";
                            String accountId = jsonNode1.has("accountId") ? jsonNode1.get("accountId").asText() : "";
                            //No need to store addon_users
                            accountIdMap.put(userKey, accountId);
                            accountIdMap.put(userName, accountId);

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
        return accountIdMap;

    }


    //Audit
    public Map<String, String> getUsersFromAudit(String auditUrl, String ctId) {
        if (!StringUtils.isEmpty(auditUrl) && !StringUtils.isEmpty(ctId)) {
            log.info("Audit url : {} , citd : {} ", auditUrl, ctId);
            Integer maxResult = 500;
            String continueStr = null;
            Map<String, String> users = new HashMap<>();
            do {
                try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

                    HttpPost post = new HttpPost(auditUrl + "/search/users/by/tenant");
                    post.setHeader("Content-Type", "application/json");
                    ObjectMapper om = new ObjectMapper();
                    ObjectNode jsonNode = createBaseAuditSearchRequest(ctId, maxResult, continueStr);
                    log.info("Request details to audit : {}", om.writeValueAsString(jsonNode));
                    StringEntity entity = new StringEntity(om.writeValueAsString(jsonNode));
                    post.setEntity(entity);

                    HttpResponse response = client.execute(post);

                    if (response.getStatusLine().getStatusCode() == 200) {
                        log.info("Successfully invoked user search on audit for tenant -> " + ctId);
                        String responseJson = IOUtils.toString(response.getEntity().getContent());
                        JsonNode resultNode = om.readTree(responseJson);
                        if (resultNode != null) {
                            continueStr = resultNode.has("continuation") ? resultNode.get("continuation").asText() : null;
                            continueStr = "null".equalsIgnoreCase(continueStr) ? null : continueStr;
                            JsonNode contentArray = resultNode.get("content");
                            if (contentArray != null) {
                                for (JsonNode node : contentArray) {
                                    String userKey = node.has("userKey") ? node.get("userKey").asText() : "";
                                    String userName = node.has("userName") ? node.get("userName").asText() : "";
                                    String accountId = node.has("accountId") ? node.get("accountId").asText() : "";
                                    if (userKey != null && userKey.length() > 0 && accountId != null && accountId.length() > 0) {
                                        users.put(userKey, accountId);
                                    }
                                    if (userName != null && userName.length() > 0 && accountId != null && accountId.length() > 0) {
                                        users.put(userName, accountId);
                                    }

                                }
                            }
                        }
                    } else {
                        log.error("Error occurred while invoking user search on audit for tenant -> " + ctId);
                        log.error(IOUtils.toString(response.getEntity().getContent()));
                    }
                } catch (Exception e) {
                    log.error("Exception caught while invoking user search on audit for tenant -> " + ctId, e);
                    continueStr = null;
                    return new HashMap<>();
                }
            }
            while (continueStr != null);
            log.info("User size : {} ", users.size());
            return users;

        }
        return new HashMap<>();
    }

    private static ObjectNode createBaseAuditSearchRequest(String ctid, Integer maxResult, String continuation) {
        ObjectMapper om = new ObjectMapper();
        ObjectNode searchRequest = om.createObjectNode();
        searchRequest.put("product", "Capture");
        searchRequest.put("tenantId", ctid);
        searchRequest.put("maxResult", maxResult);
        if (StringUtils.isNotEmpty(continuation)) searchRequest.put("continuation", continuation);
        return searchRequest;
    }

}
