package com.thed.zephyr.capture.validator;

import com.google.common.collect.Maps;
import com.thed.zephyr.capture.model.SessionRequest;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

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
				errors.rejectValue("projectKey","", i18n.getMessage("session.project.key.needed"));
			}
			if (sessionRequest.getName() !=null&& sessionRequest.getName().length() > CaptureConstants.SESSION_NAME_LENGTH_LIMIT) {
				errors.rejectValue("name","", i18n.getMessage("session.name.exceed.limit", new Integer[]{sessionRequest.getName().length(),
						CaptureConstants.SESSION_NAME_LENGTH_LIMIT}));
			}
			if (sessionRequest.getAdditionalInfo() != null && sessionRequest.getAdditionalInfo().length() > CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT) {
				errors.rejectValue("additionalInfo","", i18n.getMessage("session.additionalInfo.exceed.limit", new Integer[]{sessionRequest.getAdditionalInfo().length(),
						CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT}));
			}
			CaptureProject project = projectService.getCaptureProject(sessionRequest.getProjectKey()); //checking whether the project key is valid or not.
			if(Objects.isNull(project)) {
				errors.rejectValue("projectKey","", i18n.getMessage("session.project.key.invalid", new Object[]{sessionRequest.getProjectKey()}));
			} else {
				sessionRequest.setProjectId(project.getId());
				sessionRequest.setProjectName(project.getName());
			}
			if(!Objects.isNull(sessionRequest.getRelatedIssues())) {
				Set<Long> relatedIssues = new TreeSet<>();
			    Map<String, String> duplicatePrevention = Maps.newHashMap();
				Set<String> issuesList = sessionRequest.getRelatedIssues();
				issuesList.stream().forEach(issueKey -> {
					if (!StringUtils.isEmpty(issueKey) && !duplicatePrevention.containsKey(issueKey)) {
						CaptureIssue issue = issueService.getCaptureIssue(issueKey); //Checking whether the issues are valid or not.
						if (Objects.isNull(issue)) {
		                    errors.reject("", i18n.getMessage("session.issue.key.invalid", new Object[]{issueKey}));
		                } else {
		                	duplicatePrevention.put(issueKey, issueKey);
	                        relatedIssues.add(issue.getId());
		                }
					}
				});
				sessionRequest.setRelatedIssueIds(relatedIssues.size() > 0 ? relatedIssues : null);
				int issueLimit = Integer.parseInt(dynamicProperty.getStringProp(ApplicationConstants.RELATED_ISSUES_LIMIT_DYNAMIC_KEY, "100").get());
				if (relatedIssues.size() > issueLimit) { //checking whether related issues are crossed more than the limit.
					errors.rejectValue("relatedIssueIds","", i18n.getMessage("session.relatedissues.exceed", new Object[]{relatedIssues.size(), issueLimit}));
		        }
			}
		}
	}

}
