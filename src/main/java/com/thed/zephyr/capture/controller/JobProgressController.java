package com.thed.zephyr.capture.controller;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * @author manjunath
 *
 */
@RestController
@RequestMapping(value = "/jobprogress")
public class JobProgressController {
	
	@Autowired
	private JobProgressService jobProgressService;
	
	@Autowired
    private Logger log;
	
	@Autowired
	private CaptureI18NMessageSource captureI18NMessageSource;
	
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;
	
	@IgnoreJwt
	@GetMapping(value = "/{jobProgressTicket}")
	public ResponseEntity<?> getJobProgress(@PathVariable String jobProgressTicket, @RequestParam Optional<String> baseURL) {
		log.info("Start of getJobProgress() -> params - jobProgressId -> " + jobProgressTicket);
		try {
			AcHostModel acHostModel = null;
			if(baseURL.isPresent()) {
				acHostModel = CaptureUtil.getAcHostModel(dynamoDBAcHostRepository, baseURL.get());
			} else {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				AtlassianHostUser atlassianHostUser = (AtlassianHostUser) auth.getPrincipal();
	            acHostModel = (AcHostModel) atlassianHostUser.getHost();
			}
			Map<String, Object> progressMap = jobProgressService.checkJobProgress(acHostModel, jobProgressTicket);
            if (null == progressMap) {
            	String errorMessage = captureI18NMessageSource.getMessage("capture.job.progress.status.error");
                log.error("Can't get JobProgress from cache jobProgressTicket: " + jobProgressTicket);
                throw new CaptureRuntimeException(errorMessage);
            }			
			log.info("End of getJobProgress()");
			return ResponseEntity.ok(progressMap);
		} catch(Exception ex) {
			log.error("Error in getJobProgress() -> ", ex);
			throw new CaptureRuntimeException(ex);
		}
	}
}
