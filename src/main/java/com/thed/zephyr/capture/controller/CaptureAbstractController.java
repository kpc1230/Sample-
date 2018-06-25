package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.ErrorCollection;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract controller to use in capture and contains 
 * the required methods for all subclasses to use.
 * @author venkatareddy on 09/07/2017
 *
 */
public abstract class CaptureAbstractController {

	private static final Logger log = LoggerFactory.getLogger(CaptureAbstractController.class);


	@Autowired
	protected CaptureI18NMessageSource i18n;
	
	@Autowired
	protected SessionService sessionService;
	
	@Autowired
	private DynamoDBAcHostRepository dynamoDBAcHostRepository;

	@Autowired
	private DynamicProperty dynamicProperty;

	@Autowired
	private AmazonSEService amazonSEService;

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
		String ctId = CaptureUtil.getCurrentCtId();
		log.debug("Security context and session's ctId:{},{}", ctId, loadedSession.getCtId());
		if(!StringUtils.equals(ctId, loadedSession.getCtId())){
			log.error("Missmatch during getting session by sessionId:{}, ctId:{}",sessionId,ctId);
			Mail mail = new Mail();
			String toEmail = dynamicProperty.getStringProp(ApplicationConstants.FEEDBACK_SEND_EMAIL, "atlassian.dev@getzephyr.com").get();
			String body = "<p>Looking session by :" + ctId + " </p>";
			body += "<p>Found session with sessionId: "+loadedSession.getId()+"</p>";
			body += "<p>Found session with ctId: "+loadedSession.getCtId()+"</p>";
			mail.setTo(toEmail);
			mail.setSubject("Mismatch during retrieving session for ctId:" + ctId);
			mail.setText(body);

			try {
				if (amazonSEService.sendMail(mail)) {
					log.info("Successfully sent email to : {}", toEmail);
				}
			} catch (MessagingException e) {
				log.error("Error during sending Mismatch Session Email. for ctId:" + ctId);
			}

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
	protected ResponseEntity<?> badRequest(ErrorCollection errorCollection) {
		Map<String,Object> errorMap = new HashedMap();

		List<Map<?,?>> listOfErrors = new ArrayList<>();
		errorCollection.getErrors().stream().forEach(fieldError ->{
			Map<String,String> errorMsg = new HashedMap();
			errorMsg.put("errorMessage",fieldError.getMessage());
			listOfErrors.add(errorMsg);
		} );
		//listOfErrors.add(errorMsg);
		errorMap.put("errors",listOfErrors);

		return ResponseEntity.badRequest().body(errorMap);
	}
	
	public AcHostModel getAcHostModel() {
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findOne(atlassianHostUser.getHost().getClientKey());
        return acHostModel;
    }
}
