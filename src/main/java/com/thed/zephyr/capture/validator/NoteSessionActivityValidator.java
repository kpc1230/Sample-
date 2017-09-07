package com.thed.zephyr.capture.validator;

import java.util.Objects;

import com.thed.zephyr.capture.model.NoteSessionActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.amazonaws.util.StringUtils;
import com.thed.zephyr.capture.model.NoteRequest;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;

/**
 * Validator Class that will be invoked for create and update methods of
 * note API.
 * 
 * @author Venkatareddy on 08/28/17.
 *
 */
@Component
public class NoteSessionActivityValidator implements Validator {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private CaptureI18NMessageSource i18n;

	@Override
	public boolean supports(Class<?> clazz) {
		return NoteRequest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target instanceof NoteSessionActivity){
			NoteSessionActivity noteSessionActivity = (NoteSessionActivity) target;
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteData", "", i18n.getMessage("note.create.empty"));
			int noteLength = noteSessionActivity.getNoteData().length();
			if (noteSessionActivity.getNoteData() != null && noteLength > ApplicationConstants.MAX_NOTE_LENGTH) {
                errors.reject("", i18n.getMessage("note.exceed.limit", new Object[]{noteLength, ApplicationConstants.MAX_NOTE_LENGTH}));
            }

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sessionId", "", i18n.getMessage("session.invalid.id", new Object[]{noteSessionActivity.getSessionId()}));
			if(!StringUtils.isNullOrEmpty(noteSessionActivity.getSessionId())){
				Session s = sessionService.getSession(noteSessionActivity.getSessionId());
				if(Objects.isNull(s)) {
					errors.reject("", i18n.getMessage("session.invalid", new Object[]{noteSessionActivity.getSessionId()}));
				}else{
					noteSessionActivity.setProjectId(s.getProjectId());
				}
			}
		}		
	}

}
