package com.thed.zephyr.capture.validator;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;

/**
 * Validator Class that will be invoked for create and update methods of template API.
 * 
 * @author Venkatareddy on 08/18/17.
 *
 */
@Component
public class TemplateValidator implements Validator {
	
	@Autowired
	private ProjectService projectService;
	
	@Override
	public boolean supports(Class<?> arg0) {
		return TemplateRequest.class.equals(arg0);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		if(obj instanceof TemplateRequest){
			TemplateRequest templateReq = (TemplateRequest) obj;
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "", "Template name can't be empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ownerName", "", "Template Owner name can't be empty");
			Project project = projectService.getProjectObj(templateReq.getProjectId());
			if(Objects.isNull(project)) {
				errors.reject("", "Project is not valid");
			} else {
				templateReq.setProjectKey(project.getKey());
			}
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "issueType", "", "Issue Type can't be empty");
		}
	}

}
