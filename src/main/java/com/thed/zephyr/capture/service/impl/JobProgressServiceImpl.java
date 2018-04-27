package com.thed.zephyr.capture.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.exception.JobFailedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.view.JobProgress;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.UniqueIdGenerator;

/**
 * @author manjunath
 * @see com.thed.zephyr.capture.service.JobProgressService
 *
 */
@Service
public class JobProgressServiceImpl implements JobProgressService {
	
	@Autowired
	private ITenantAwareCache iTenantAwareCache;
	
	@Autowired
	private LockService lockService;
	
	@Autowired
	private Logger log;
	
	@Autowired
    private HazelcastInstance hazelcastInstance;
	
	@Override
    public JobProgress createJobProgress(AcHostModel acHostModel, String name, Integer status, String jobProgressTicket) throws HazelcastInstanceNotDefinedException {
        jobProgressTicket = StringUtils.isNotBlank(jobProgressTicket) ? jobProgressTicket : new UniqueIdGenerator().getStringId();
        JobProgress jobProgress = new JobProgress(jobProgressTicket, name, status, new Date(), acHostModel.getClientKey(), 1, 0);
        setJobProgressToCache(acHostModel, jobProgressTicket, jobProgress);
        log.info("Job Progress instance was created and pushed to cache jobProgressTicket: " + jobProgressTicket + " jobName: " + name + " tenantId: " + acHostModel.getClientKey());
        return jobProgress;
    }

    @Override
    public JobProgress createJobProgress(AcHostModel acHostModel, String name, Integer status) throws HazelcastInstanceNotDefinedException {
        String jobProgressTicket = new UniqueIdGenerator().getStringId();
        JobProgress jobProgress = new JobProgress(jobProgressTicket, name, status, new Date(), acHostModel.getClientKey(), 1, 0);
        setJobProgressToCache(acHostModel, jobProgressTicket, jobProgress);
        log.info("Job Progress instance was created and pushed to cache jobProgressTicket: " + jobProgressTicket + " jobName: " + name + " tenantId: " + acHostModel.getClientKey());
        return jobProgress;
    }

    @Override
    public JobProgress completedWithStatus(AcHostModel acHostModel, Integer status, String jobProgressTicket) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, jobProgressTicket);
        if (jobProgress != null) {
            jobProgress.setStatus(status);
            jobProgress.setEndTime(new Date());
            setJobProgressToCache(acHostModel, jobProgressTicket, jobProgress);
            log.info("Job Progress was completed jobProgressTicket: " + jobProgressTicket + " jobName: " + jobProgress.getName() + " status: " + status + " tenantId: " + acHostModel.getClientKey());
        }
        return jobProgress;
    }

    @Override
    public JobProgress setTotalSteps(AcHostModel acHostModel, String cacheKey, Integer totalSteps) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setTotalSteps(totalSteps);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public Integer getTotalSteps(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        Integer message = null;
        if (jobProgress != null) {
            message = jobProgress.getTotalSteps();
        }
        return message;
    }

    @Override
    public JobProgress addSteps(AcHostModel acHostModel, String cacheKey, Integer steps) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setTotalSteps(jobProgress.getTotalSteps() + steps);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress setCompletedSteps(AcHostModel acHostModel, String cacheKey, Integer completedSteps) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setCompletedSteps(completedSteps);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public Integer getCompletedSteps(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        Integer message = null;
        if (jobProgress != null) {
            message = jobProgress.getCompletedSteps();
        }
        return message;
    }

    @Override
    public JobProgress addCompletedSteps(AcHostModel acHostModel, String cacheKey, Integer completedSteps) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setCompletedSteps(jobProgress.getCompletedSteps() + completedSteps);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress addCompletedSteps(AcHostModel acHostModel, String cacheKey, JobProgress jobProgress, Integer completedSteps) throws HazelcastInstanceNotDefinedException {
        if (jobProgress != null) {
            jobProgress.setCompletedSteps(jobProgress.getCompletedSteps() + completedSteps);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress setMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setMessage(message);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress setStepMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setStepMessage(message);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public String getStepMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        String message = null;
        if (jobProgress != null) {
            message = jobProgress.getStepMessage();
        }
        return message;
    }

    @Override
    public JobProgress addStepMessages(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.addStepMassages(message);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress addCurrentStepMessageToMessages(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.addStepMassages(jobProgress.getStepMessage());
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public List<String> getStepMessages(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        List<String> message = null;
        if (jobProgress != null) {
            message = jobProgress.getStepMessages();
        }
        return message;
    }

    @Override
    public JobProgress setStepLabel(AcHostModel acHostModel, String cacheKey, String label) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setStepLabel(label);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public String getStepLabel(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        String message = null;
        if (jobProgress != null) {
            message = jobProgress.getStepLabel();
        }
        return message;
    }

    @Override
    public String getSummaryMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        String message = null;
        if (jobProgress != null) {
            message = jobProgress.getSummaryMessage();
        }
        return message;
    }

    @Override
    public JobProgress setSummaryMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setSummaryMessage(message);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public String getErrorMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        String message = null;
        if (jobProgress != null) {
            message = jobProgress.getErrorMessage();
        }
        return message;
    }

    @Override
    public JobProgress setErrorMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setErrorMessage(message);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public JobProgress cancelJob(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress != null) {
            jobProgress.setCanceledJob(true);
            setJobProgressToCache(acHostModel, cacheKey, jobProgress);
        }
        return jobProgress;
    }

    @Override
    public Boolean isJobCanceled(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        Boolean isCanceled = null;
        if (jobProgress != null) {
            isCanceled = jobProgress.getCanceledJob();
        }
        return isCanceled;
    }

    @Override
    public Map<String, Object> checkJobProgress(AcHostModel acHostModel, String cacheKey) throws JobFailedException, HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = getJobProgress(acHostModel, cacheKey);
        if (jobProgress == null){
            return null;
        }
        Double progress = jobProgress.getProgress();
        if (ApplicationConstants.JOB_STATUS_COMPLETED == jobProgress.getStatus()) {
            progress = 1.0;
            iTenantAwareCache.remove(acHostModel, cacheKey);
        } else if (ApplicationConstants.JOB_STATUS_FAILED == jobProgress.getStatus()) {
        	iTenantAwareCache.remove(acHostModel, cacheKey);
            String errorMessage = jobProgress.getErrorMessage() != null? jobProgress.getErrorMessage() : "zephyr.common.internal.server.error";
            throw new JobFailedException(errorMessage);
        } else if (ApplicationConstants.JOB_STATUS_INPROGRESS == jobProgress.getStatus()) {
            if (progress == 1.0) {
                progress = 0.99;
            }
        }

        return convertJobProgressToMap(jobProgress, progress);
    }

    @Override
    public Map<String, Object> convertJobProgressToMap(JobProgress jobProgress, Double progress) {
        if (progress == null) {
            progress = jobProgress.getProgress();
        }
        Map<String, Object> progressMap = Maps.newHashMap();
        progressMap.put("timeTaken", jobProgress.getTimeSpend() != null ? jobProgress.getTimeSpend() : "");
        progressMap.put("stepMessage", jobProgress.getStepMessage() != null ? jobProgress.getStepMessage() : "");
        progressMap.put("summaryMessage", jobProgress.getSummaryMessage() != null ? jobProgress.getSummaryMessage() : "");
        progressMap.put("stepMessages", jobProgress.getStepMessages());
        progressMap.put("progress", progress);
        progressMap.put("message", jobProgress.getMessage() != null ? jobProgress.getMessage() : "");
        progressMap.put("errorMessage", jobProgress.getErrorMessage() != null ? jobProgress.getErrorMessage() : "");
        progressMap.put("stepLabel", jobProgress.getStepLabel() != null ? jobProgress.getStepLabel() : "");

        return progressMap;
    }

    @Override
    public void removeJobProgress(AcHostModel acHostModel, String cacheKey) {
        iTenantAwareCache.remove(acHostModel, cacheKey);
    }

    private JobProgress getJobProgress(AcHostModel acHostModel, String jobProgressTicket) throws HazelcastInstanceNotDefinedException {
        if (StringUtils.isBlank(jobProgressTicket)){
            return null;
        }
        IMap<String, JobProgress> inProgressJobsMap = hazelcastInstance.getMap(ApplicationConstants.IN_PROGRESS_JOBS_MAP);
        String key = getIMapKey(acHostModel, jobProgressTicket);
        JobProgress jobProgress = inProgressJobsMap.get(key);
        if (jobProgress == null){
            IMap<String, JobProgress> completedProgressJobsMap = hazelcastInstance.getMap(ApplicationConstants.COMPLETED_JOBS_MAP);
            jobProgress = completedProgressJobsMap.get(key);
        }
        return jobProgress;
    }

    private void setJobProgressToCache(AcHostModel acHostModel, String jobProgressTicket, JobProgress jobProgress) throws HazelcastInstanceNotDefinedException{
        if (StringUtils.isBlank(jobProgressTicket)){
            return;
        }
        IMap<String, JobProgress> jobsMap;
        String key = getIMapKey(acHostModel, jobProgressTicket);
        if (jobProgress.getStatus() == ApplicationConstants.JOB_STATUS_COMPLETED || jobProgress.getStatus() == ApplicationConstants.JOB_STATUS_FAILED){
            jobsMap = hazelcastInstance.getMap(ApplicationConstants.COMPLETED_JOBS_MAP);
            jobsMap.set(key, jobProgress, ApplicationConstants.DEFAULT_EXPIRE_TIME_FOR_COMPLETED_JOB_PROGRESS, TimeUnit.HOURS);
            removeJobProgress(acHostModel, jobProgressTicket, ApplicationConstants.IN_PROGRESS_JOBS_MAP);
            cleanCompletedJobsMap(acHostModel, jobsMap);
        } else {
            jobsMap = hazelcastInstance.getMap(ApplicationConstants.IN_PROGRESS_JOBS_MAP);
            jobsMap.set(key, jobProgress, ApplicationConstants.DEFAULT_EXPIRE_TIME_FOR_IN_PROGRESS_JOB_PROGRESS, TimeUnit.HOURS);
        }
    }

    private void removeJobProgress(AcHostModel acHostModel, String jobProgressTicket, String iMapName)  throws HazelcastInstanceNotDefinedException{
        IMap<String, JobProgress> jobsMap = hazelcastInstance.getMap(iMapName);
        String key = getIMapKey(acHostModel, jobProgressTicket);
        jobsMap.delete(key);
        log.info("Job Progress was deleted from cache iMap: " + iMapName + " jobProgressTicket: " + jobProgressTicket + " tenantId: " + acHostModel.getClientKey());
    }

    @SuppressWarnings("rawtypes")
	private void cleanCompletedJobsMap(AcHostModel acHostModel, final IMap<String, JobProgress> jobsMap) throws HazelcastInstanceNotDefinedException {
        if (jobsMap.size() < ApplicationConstants.DEFAULT_COMPLETED_JOB_MAP_SIZE){
            return;
        }
        String lockKey = ApplicationConstants.CLEAN_JOB_PROGRESS_COMPLETED_MAP_LOCK_KEY;
        if (!lockService.tryLock(acHostModel.getClientKey(), lockKey, 1)){
            return;
        }
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        CompletableFuture.runAsync(() -> {
            CaptureUtil.putAcHostModelIntoContext((AcHostModel) hostUser.getHost(), hostUser.getUserKey().get());
        	final Long currentTime = new Date().getTime();
            try {
            	EntryObject entryObject = new PredicateBuilder().getEntryObject();
                Predicate predicate = entryObject.get("endTime").lessThan(currentTime - ApplicationConstants.MAX_LIFE_TIME_FOR_JOBS_DURING_CLEAN_COMPLETED_JOB_MAP);
                Set<Map.Entry<String, JobProgress>> removedJobSet = jobsMap.entrySet(predicate);
                log.info("Clean completed job progress iMap " + removedJobSet.size() + " elements will be removed.");
                for (Map.Entry<String, JobProgress> entry:removedJobSet){
                    jobsMap.delete(entry.getKey());
                }
                lockService.deleteLock(acHostModel.getClientKey(), lockKey);
            } catch(Exception ex) {
            	log.error("Error during clean job progress completed iMap.", ex);
                try {
					lockService.deleteLock(acHostModel.getClientKey(), lockKey);
				} catch (HazelcastInstanceNotDefinedException e) {
					log.error("Error in cleanCompletedJobsMap() - deleting key -> " + lockKey, ex);
				}
            }
        });
    }

    private String getIMapKey(AcHostModel acHostModel, String jobProgressTicket) {
        return String.valueOf(acHostModel.getClientKey() + "_" + jobProgressTicket);
    }
}
