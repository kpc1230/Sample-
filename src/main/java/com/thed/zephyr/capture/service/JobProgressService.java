package com.thed.zephyr.capture.service;

import java.util.List;
import java.util.Map;

import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.exception.JobFailedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.view.JobProgress;

/**
 * @author manjunath
 *
 */
public interface JobProgressService {
	
	JobProgress createJobProgress(AcHostModel acHostModel, String name, Integer status, String jobProgressTicket) throws HazelcastInstanceNotDefinedException;

    JobProgress createJobProgress(AcHostModel acHostModel, String name, Integer status) throws HazelcastInstanceNotDefinedException;

    JobProgress completedWithStatus(AcHostModel acHostModel, Integer status, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress setTotalSteps(AcHostModel acHostModel, String cacheKey, Integer totalSteps) throws HazelcastInstanceNotDefinedException;

    Integer getTotalSteps(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress addSteps(AcHostModel acHostModel, String cacheKey, Integer steps) throws HazelcastInstanceNotDefinedException;

    JobProgress setCompletedSteps(AcHostModel acHostModel, String cacheKey, Integer completedSteps) throws HazelcastInstanceNotDefinedException;

    Integer getCompletedSteps(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress addCompletedSteps(AcHostModel acHostModel, String cacheKey, Integer completedSteps) throws HazelcastInstanceNotDefinedException;

    JobProgress addCompletedSteps(AcHostModel acHostModel, String cacheKey, JobProgress jobProgress, Integer completedSteps) throws HazelcastInstanceNotDefinedException;

    JobProgress setMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException;

    JobProgress setStepMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException;

    String getStepMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress addStepMessages(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException;

    JobProgress addCurrentStepMessageToMessages(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    List<String> getStepMessages(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress setStepLabel(AcHostModel acHostModel, String cacheKey, String label) throws HazelcastInstanceNotDefinedException;

    String getStepLabel(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    String getSummaryMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress setSummaryMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException;

    String getErrorMessage(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    JobProgress setErrorMessage(AcHostModel acHostModel, String cacheKey, String message) throws HazelcastInstanceNotDefinedException;

    JobProgress cancelJob(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    Boolean isJobCanceled(AcHostModel acHostModel, String cacheKey) throws HazelcastInstanceNotDefinedException;

    Map<String, Object> checkJobProgress(AcHostModel acHostModel, String cacheKey) throws JobFailedException, HazelcastInstanceNotDefinedException;

    Map<String, Object> convertJobProgressToMap(JobProgress jobProgress, Double progress);

    void removeJobProgress(AcHostModel acHostModel, String cacheKey);
    
}
