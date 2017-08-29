package com.thed.zephyr.capture.validator;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	
	@Autowired
	private IssueService issueService;

	@Override
	public boolean supports(Class<?> clazz) {
		return SessionRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target  instanceof SessionRequest) {
			SessionRequest sessionRequest = (SessionRequest) target;
			if(Objects.isNull(sessionRequest.getProjectKey())) {
				errors.reject("", "Project key cannot be empty");
			}
			Project project = projectService.getProjectObjByKey(sessionRequest.getProjectKey()); //checking whether the project key is valid or not.
			if(Objects.isNull(project)) {
				errors.reject("", "Not a valid project");
			} else {
				sessionRequest.setProjectId(project.getId());
			}
			if(!Objects.isNull(sessionRequest.getRelatedIssues())) { 
				List<Long> relatedIssues = Lists.newArrayList();
			    Map<String, String> duplicatePrevention = Maps.newHashMap();
 				List<String> issuesList = sessionRequest.getRelatedIssues();
				issuesList.stream().forEach(issueKey -> {
					if (!StringUtils.isEmpty(issueKey) && !duplicatePrevention.containsKey(issueKey)) {
						Issue issue = issueService.getIssueObject(issueKey); //Checking whether the issues are valid or not.
						if (Objects.isNull(issue)) {
		                    errors.reject("", "Issue with key " + issueKey + " cannot be found.");
		                } else {
		                	duplicatePrevention.put(issueKey, issueKey);
	                        relatedIssues.add(issue.getId());
		                }
					}
				});
				sessionRequest.setRelatedIssueIds(relatedIssues);
				if (relatedIssues.size() > ApplicationConstants.RELATED_ISSUES_LIMIT) { //checking whether related issues are crossed more than the limit.
					errors.reject("", MessageFormat.format("There are {0} related issues. This session cannot have more than {1} related issues.", relatedIssues.size(), ApplicationConstants.RELATED_ISSUES_LIMIT));
		        }
			}
		}
	}

}
