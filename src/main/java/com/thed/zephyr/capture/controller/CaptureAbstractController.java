package com.thed.zephyr.capture.controller;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.model.ErrorDto;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.ErrorCollection;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;

/**
 * Abstract controller to use in capture and contains 
 * the required methods for all subclasses to use.
 * @author venkatareddy on 09/07/2017
 *
 */
public abstract class CaptureAbstractController {

	@Autowired
	protected CaptureI18NMessageSource i18n;
	
	@Autowired
	protected SessionService sessionService;
	
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;

	/**
	 * Fetches the user key from the authentication object.
	 *
	 * @return -- Returns the logged in user key.
	 * @throws CaptureValidationException -- Thrown while fetching the user key.
	 */
	protected String getUser() throws CaptureValidationException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String userKey = host.getUserKey().get();
		if(StringUtils.isBlank(userKey)) {
			throw new CaptureValidationException(i18n.getMessage("rest.resource.user.not.authenticated"));
		}
		return userKey;
	}
	
	/**
	 * Validates the session.
	 * 
	 * @param sessionId -- Session id requested by user
	 * @return -- Returns the validated Session object using the session id.
	 * @throws CaptureValidationException -- Thrown while validating the session.
	 */
	protected Session validateAndGetSession(String sessionId) throws CaptureValidationException {
		if(StringUtils.isEmpty(sessionId)) {
			throw new CaptureValidationException(i18n.getMessage("session.invalid.id", new Object[]{sessionId}));
		}
		Session loadedSession = sessionService.getSession(sessionId);
		if(Objects.isNull(loadedSession)) {
			throw new CaptureValidationException(i18n.getMessage("session.invalid", new Object[]{sessionId}));
		}
		return loadedSession;
	}
	
	/**
	 * Constructs the bad request response entity for the validation errors.
	 * 
	 * @param errorCollection -- Holds the validation errors information.
	 * @return -- Returns the constructed response entity.
	 */
	protected ResponseEntity<List<ErrorDto>> badRequest(ErrorCollection errorCollection) {		
		return ResponseEntity.badRequest().body(errorCollection.toErrorDto());
	}
	
	public AcHostModel getAcHostModel() {
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findOne(atlassianHostUser.getHost().getClientKey());
        return acHostModel;
    }
}
