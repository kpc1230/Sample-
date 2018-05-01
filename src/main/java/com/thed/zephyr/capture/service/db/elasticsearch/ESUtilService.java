package com.thed.zephyr.capture.service.db.elasticsearch;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.NoteRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.LicenseService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ESUtilService {

    @Autowired
    private AtlassianHostRepository atlassianHostRepository;
    @Autowired
    private Client client;
    @Autowired
    private Logger log;
    @Autowired
    private JobProgressService jobProgressService;
    @Autowired
    private LockService lockService;
    @Autowired
    private CaptureI18NMessageSource captureI18NMessageSource;
    @Autowired
    private SessionESRepository sessionESRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private SessionActivityRepository sessionActivityRepository;
    @Autowired
    private LicenseService licenseService;

    public void createAliases(){
        Iterable<AtlassianHost> allHosts = atlassianHostRepository.findAll();
        for (AtlassianHost host:allHosts){
            AcHostModel acHostModel = (AcHostModel)host;
            createAlias(acHostModel.getCtId());
        }
    }

    public Boolean isAliasExist(String aliasName){
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest();
        getAliasesRequest.aliases(aliasName);
        AliasesExistResponse aliasesExistResponse = client.admin().indices().aliasesExist(getAliasesRequest).actionGet();
        return  aliasesExistResponse.isExists();
    }

    public void createAlias(String aliasName){
        if(!isAliasExist(aliasName)){
            IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
            AliasAction action = new AliasAction(AliasAction.Type.ADD);
            action.index(ApplicationConstants.ES_INDEX_NAME).alias(aliasName).indexRouting(aliasName);
            QueryBuilder queryBuilder = new TermQueryBuilder(ApplicationConstants.TENANT_ID_FIELD, aliasName);
            action.filter(queryBuilder);
            indicesAliasesRequest.addAliasAction(action);
            client.admin().indices().aliases(indicesAliasesRequest).actionGet();
            log.info("Elasticsearch alias created for tenant ctId:{}", aliasName);
        } else{
            log.info("Elasticsearch alias already exist for tenant ctId:{}", aliasName);
        }
    }

    public void reindexESCluster(){
        long startTime = new Date().getTime();
        int poolSize = dynamicProperty.getIntProp(ApplicationConstants.REINDEX_ES_CLUSTER_THREAD_POOL_SIZE_DYNAMIC_PROP, ApplicationConstants.DEFAULT_REINDEX_ES_CLUSTER_THREAD_POOL_SIZE).get();
        BlockingPool blockingPool = new BlockingPool(poolSize);
        Iterable<AtlassianHost> allHosts = atlassianHostRepository.findAll();
        int count = 0;
        for (AtlassianHost host:allHosts){
            count++;
            if(((AcHostModel)host).getStatus() != AcHostModel.TenantStatus.ACTIVE){
                continue;
            }
            try {
                blockingPool.takeJob();
                reindexTenantESData((AcHostModel)host, null, "system", blockingPool);
                log.info("Re-indexed {} tenants",count);
            } catch (HazelcastInstanceNotDefinedException e) {
                log.error("Error during whole ES cluser reindex for tenant ctId:{}", ((AcHostModel)host).getCtId(), e);
            }
        }
        blockingPool.isPoolFull();
        long duration = new Date().getTime() - startTime;
        log.info("Elasticsearch cluster reindex done duration:{}", duration );
    }

    public void reindexTenantESData(AcHostModel acHostModel, String jobProgressId, String userName, BlockingPool blockingPool) throws HazelcastInstanceNotDefinedException {
        jobProgressService.createJobProgress(acHostModel, ApplicationConstants.REINDEX_CAPTURE_ES_DATA, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        CompletableFuture.runAsync(() -> {
            CaptureUtil.putAcHostModelIntoContext(acHostModel, userName);
            try {
                LicenseService.Status licenseStatus = licenseService.getLicenseStatus();
                if(licenseStatus != LicenseService.Status.ACTIVE){
                    return;
                }
                if (!lockService.tryLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA, 5)){
                    log.warn("Re-index sessions process already in progress for tenant ctId:{}", acHostModel.getCtId());
                    jobProgressService.setErrorMessage(acHostModel, jobProgressId, captureI18NMessageSource.getMessage("capture.admin.plugin.test.section.item.zephyr.configuration.reindex.executions.inprogress"));
                    return;
                }
                log.debug("Re-Indexing Session type data begin:");
                long startTime = new Date().getTime();
                cleanESData();
                reindexSessionsWithNotes(acHostModel, jobProgressId);
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_COMPLETED, jobProgressId);
                long duration = new Date().getTime() - startTime;
                log.info("Was re-indexed {} sessions for tenant ctId:{} duration:{}", jobProgressService.getTotalSteps(acHostModel, jobProgressId), acHostModel.getCtId(), duration);
                String message = captureI18NMessageSource.getMessage("capture.job.progress.status.success.message");
                jobProgressService.setMessage(acHostModel, jobProgressId, message);
            } catch(Exception ex) {
                if(StringUtils.equals("503 Service Temporarily Unavailable", ex.getMessage())){
                    log.info("Jira is not available, skip the reindex ctId:{}", acHostModel.getCtId());
                } else{
                    log.error("Error during reindex for tenant ctId:{}", acHostModel.getCtId(), ex);
                }
                try {
                    jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                    String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
                    jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
                } catch (HazelcastInstanceNotDefinedException exception) {
                    log.error("Error during deleting reindex job progress for tenant ctId:{}", acHostModel.getCtId(), exception);
                }
            } finally {
                if(blockingPool != null){
                    blockingPool.releaseJob();
                }
                try {
                    lockService.deleteLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA);
                } catch (HazelcastInstanceNotDefinedException exception) {
                    log.error("Error during clearing reindex lock for tenant ctId:{}", acHostModel.getCtId(), exception);
                }
            }
        });
    }

    private void cleanESData() {
        sessionESRepository.deleteAll();
        noteRepository.deleteAll();
        log.info("Successfully deleted all the sessions and notes during reindex ctId:{}", CaptureUtil.getCurrentCtId());
    }

    private void reindexSessionsWithNotes(AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        int maxResults = dynamicProperty.getIntProp(ApplicationConstants.MAX_SESSION_REINDEX_DYNAMIC_PROP, ApplicationConstants.DEFAULT_SESSION_REINDEX).get();
        int offset = 0;
        int notesCount = 0;
        Page<Session> pageResponse;
        do{
            pageResponse = sessionRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, maxResults));
            jobProgressService.setTotalSteps(acHostModel, jobProgressId, Long.valueOf(pageResponse.getTotalElements()).intValue());
            notesCount = notesCount + reindexSessionList(acHostModel, pageResponse.getContent(), jobProgressId);
            offset++;
        }while (pageResponse.getContent().size() > 0);
        log.info("Was re-indexed {} Notes for tenant ctId:{}", notesCount, acHostModel.getCtId());
    }

    private int reindexSessionList(AcHostModel acHostModel, List<Session> sessions, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        CaptureProject project;
        CaptureUser user;
        int notesCount = 0;
        for (Session session:sessions){
            String projectId = String.valueOf(session.getProjectId());
            project = projectService.getCaptureProjectViaAddon(acHostModel, projectId);
            user = userService.findUserByKey(acHostModel, session.getAssignee());
            session.setProjectName(project != null?project.getName():"not found id=" + projectId);
            session.setUserDisplayName(user != null ? user.getDisplayName() : session.getAssignee());
            session.setStatusOrder(session.getStatus().getOrder());
            sessionESRepository.save(session);
            int sessionNotesCount = reindexNotes(session);
            notesCount = notesCount + sessionNotesCount;
            jobProgressService.addCompletedSteps(acHostModel, jobProgressId,  1);
        }
        return notesCount;
    }

    private int reindexNotes(Session session){
        int notesCount = 0;
        List<SessionActivity> sessionActivities = sessionActivityRepository.findBySessionId(session.getId());
        for (SessionActivity sessionActivity:sessionActivities){
            if(sessionActivity instanceof NoteSessionActivity){
                notesCount++;
                noteRepository.save(new Note((NoteSessionActivity)sessionActivity));
            }
        }

        return notesCount;
    }
}
