package com.thed.zephyr.capture.validator;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;

/**
 * Class will be invoked when session related create and update API's are called.
 * It simply validates the input parameters.
 * 
 * @author manjunath
 *
 */
@Component
public class SessionValidator implements Validator {
	
	@Autowired
	private ProjectService projectService;

	@Override
	public boolean supports(Class<?> clazz) {
		return Session.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target  instanceof Session) {
			Session session = (Session) target;
			if(Objects.isNull(session.getRelatedProject().getProjectTypeKey())) {
				errors.reject("", "Project key cannot be empty");
			}
			Project project = projectService.getProjectObjByKey(session.getRelatedProject().getKey());
			if(Objects.isNull(project)) {
				errors.reject("", "Not a valid project");
			}
		}
	}

}
