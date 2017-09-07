package com.thed.zephyr.capture.validator;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.DynamicProperty;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.*;

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
	
	@Autowired
    private DynamicProperty dynamicProperty;

	@Autowired
	private CaptureI18NMessageSource i18n;

	@Override
	public boolean supports(Class<?> clazz) {
		return SessionRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target  instanceof SessionRequest) {
			SessionRequest sessionRequest = (SessionRequest) target;
			if(Objects.isNull(sessionRequest.getProjectKey())) {
				errors.reject("", i18n.getMessage("session.project.key.needed"));
			}
			Project project = projectService.getProjectObjByKey(sessionRequest.getProjectKey()); //checking whether the project key is valid or not.
			if(Objects.isNull(project)) {
				errors.reject("", i18n.getMessage("session.project.key.invalid", new Object[]{sessionRequest.getProjectKey()}));
			} else {
				sessionRequest.setProjectId(project.getId());
			}
			if(!Objects.isNull(sessionRequest.getRelatedIssues())) {
				Set<Long> relatedIssues = new TreeSet<>();
			    Map<String, String> duplicatePrevention = Maps.newHashMap();
				Set<String> issuesList = sessionRequest.getRelatedIssues();
				issuesList.stream().forEach(issueKey -> {
					if (!StringUtils.isEmpty(issueKey) && !duplicatePrevention.containsKey(issueKey)) {
						Issue issue = issueService.getIssueObject(issueKey); //Checking whether the issues are valid or not.
						if (Objects.isNull(issue)) {
		                    errors.reject("", i18n.getMessage("session.issue.key.invalid", new Object[]{issueKey}));
		                } else {
		                	duplicatePrevention.put(issueKey, issueKey);
	                        relatedIssues.add(issue.getId());
		                }
					}
				});
				sessionRequest.setRelatedIssueIds(relatedIssues);
				int issueLimit = Integer.parseInt(dynamicProperty.getStringProp(ApplicationConstants.RELATED_ISSUES_LIMIT_DYNAMIC_KEY, "100").get());
				if (relatedIssues.size() > issueLimit) { //checking whether related issues are crossed more than the limit.
					errors.reject("", i18n.getMessage("session.relatedissues.excee", new Object[]{relatedIssues.size(), issueLimit}));
		        }
			}
		}
	}

}
