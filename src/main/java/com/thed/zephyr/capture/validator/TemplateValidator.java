package com.thed.zephyr.capture.validator;

import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

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
			CaptureProject project = projectService.getCaptureProject(templateReq.getProjectId());
			if(Objects.isNull(project)) {
				errors.rejectValue(TemplateRequest.FIELD_PROJECTID, "", "Project is not valid");
			} else {
				templateReq.setProjectKey(project.getKey());
			}
		}
	}

}
