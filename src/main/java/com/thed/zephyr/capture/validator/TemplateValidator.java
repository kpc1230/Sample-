package com.thed.zephyr.capture.validator;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
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
	
	@Autowired
	private IssueTypeService issueTypeService;
	
	@Override
	public boolean supports(Class<?> arg0) {
		return TemplateRequest.class.equals(arg0);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		if(obj instanceof TemplateRequest){
			TemplateRequest templateReq = (TemplateRequest) obj;
			Project project = projectService.getProjectObj(templateReq.getProjectId());
			if(Objects.isNull(project)) {
				errors.rejectValue(TemplateRequest.FIELD_PROJECTID, "", "Project is not valid");
			} else {
				templateReq.setProjectKey(project.getKey());
			}
			List<IssueType> issueTypes = issueTypeService.getIssueTypes();
			if(Objects.isNull(issueTypes) 
					|| !issueTypes.parallelStream().filter(i -> i.getId().equals(templateReq.getIssueType())).findFirst().isPresent() ) {
				errors.rejectValue(TemplateRequest.FIELD_ISSUETYPE, "", "IssueType is not valid");
			}
		}
	}

}
