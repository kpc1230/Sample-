package com.thed.zephyr.capture.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureValidationException;
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
}
