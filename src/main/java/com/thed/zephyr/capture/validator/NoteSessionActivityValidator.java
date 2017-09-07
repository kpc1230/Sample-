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
	
	@Override
	public boolean supports(Class<?> clazz) {
		return NoteRequest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target instanceof NoteSessionActivity){
			NoteSessionActivity noteSessionActivity = (NoteSessionActivity) target;
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteData", "", "Note data can't be empty");
			if (noteSessionActivity.getNoteData() != null && noteSessionActivity.getNoteData().length() > ApplicationConstants.MAX_NOTE_LENGTH) {
                errors.reject("", "Notedata can't be greater than the size:" + ApplicationConstants.MAX_NOTE_LENGTH) ;
            }

			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "sessionId", "", "Note sessionId can't be empty");
			if(!StringUtils.isNullOrEmpty(noteSessionActivity.getSessionId())){
				Session s = sessionService.getSession(noteSessionActivity.getSessionId());
				if(Objects.isNull(s)) {
					errors.reject("", "Session is invalid for the Note");
				}else{
					noteSessionActivity.setProjectId(s.getProjectId());
				}
			}
		}		
	}

}
