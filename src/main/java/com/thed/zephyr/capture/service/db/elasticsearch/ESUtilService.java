package com.thed.zephyr.capture.service.db.elasticsearch;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
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

    public void reindexSessionDataIntoES(AcHostModel acHostModel, String jobProgressId, String ctId, String userName) throws HazelcastInstanceNotDefinedException {
        jobProgressService.createJobProgress(acHostModel, ApplicationConstants.REINDEX_CAPTURE_ES_DATA, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        CompletableFuture.runAsync(() -> {
            CaptureUtil.putAcHostModelIntoContext(acHostModel, userName);
            try {
                if (!lockService.tryLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA, 5)){
                    log.warn("Re-index sessions process already in progress for tenant ctId:{}", ctId);
                    jobProgressService.setErrorMessage(acHostModel, jobProgressId, captureI18NMessageSource.getMessage("capture.admin.plugin.test.section.item.zephyr.configuration.reindex.executions.inprogress"));
                    return;
                }
                log.debug("Re-Indexing Session type data begin:");
                deleteSessionDataForCtid(ctId);
                loadSessionDataFromDBToES(acHostModel, jobProgressId);
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_COMPLETED, jobProgressId);
                String message = captureI18NMessageSource.getMessage("capture.job.progress.status.success.message");
                jobProgressService.setMessage(acHostModel, jobProgressId, message);
            } catch(Exception ex) {
                log.error("Error during reindex for tenant ctId:{}", ctId, ex);
                try {
                    jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                    String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
                    jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
                } catch (HazelcastInstanceNotDefinedException exception) {
                    log.error("Error during deleting reindex job progress for tenant ctId:{}", ctId, exception);
                }
            } finally {
                try {
                    lockService.deleteLock(acHostModel.getClientKey(), ApplicationConstants.REINDEX_CAPTURE_ES_DATA);
                } catch (HazelcastInstanceNotDefinedException exception) {
                    log.error("Error during clearing reindex lock for tenant ctId:{}", ctId, exception);
                }
            }
        });
    }

    private void deleteSessionDataForCtid(String ctid) {
        sessionESRepository.deleteSessionsByCtId(ctid);
        log.info("Successfully deleted all the sessions related to tenant id -> " + ctid);
    }

    private void loadSessionDataFromDBToES(AcHostModel acHostModel, String jobProgressId) throws HazelcastInstanceNotDefinedException {
        int maxResults = 20;
        Long total = 0L;
        int index = 0;
        Page<Session> pageResponse = loadSessionDataIntoES(acHostModel, jobProgressId, index, maxResults);
        log.debug("Session reindex: getting sessions page size:{} ctId:{}", pageResponse.getTotalElements(), acHostModel.getCtId());
        total = pageResponse.getTotalElements();
        index = index + maxResults;
        jobProgressService.setTotalSteps(acHostModel, jobProgressId, total.intValue());
        jobProgressService.addCompletedSteps(acHostModel, jobProgressId, pageResponse.getNumberOfElements());
        while(index < total.intValue()) {
            pageResponse = loadSessionDataIntoES(acHostModel, jobProgressId, index, maxResults);
            log.debug("Session reindex: getting sessions page size:{} ctId:{}", pageResponse.getTotalElements(), acHostModel.getCtId());
            index = index + maxResults;
            jobProgressService.addCompletedSteps(acHostModel, jobProgressId,  pageResponse.getNumberOfElements());
        }
    }

    private Page<Session> loadSessionDataIntoES(AcHostModel acHostModel, String jobProgressId, int index, int maxResults) {
        CaptureProject project = null;
        CaptureUser user = null;
        Page<Session> pageResponse = sessionRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(index / maxResults, maxResults));
        for(Session session : pageResponse.getContent()) {
            project = projectService.getCaptureProjectViaAddon(acHostModel, String.valueOf(session.getProjectId()));
            if(project != null){
                user = userService.findUserByKey(acHostModel, session.getAssignee());
                session.setProjectName(project.getName());
                session.setUserDisplayName(user != null ? user.getDisplayName() : session.getAssignee());
                session.setStatusOrder(getStatusOrder(session.getStatus()));
                sessionESRepository.save(session);
            }
        }
        return pageResponse;
    }
}
