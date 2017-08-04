package com.atlassian.jira.ext.rest;

import com.atlassian.bonfire.events.RestCreateIssueEvent;
import com.atlassian.bonfire.service.controller.AdvancedCFController;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.ext.issue.IssueInputParametersExt;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.status;

/**
 * <p>
 * Extensions to the JIRA issue resource. This code should eventually be moved into JIRA and was
 * however it has since progressed and getting backports and the like is no mean feat.
 * </p>
 * <p>
 * So we have kept the duplicated code and now consider it to be Bonfire code. Until such time
 * as we cam move exclusively JIRA 5.0 this code will exists and perhaps even longer.
 * </p>
 * <p>
 * Share nothing!  It usually works out best that way.
 * </p>
 */
public class IssueExt {
    private static final Logger log = Logger.getLogger(IssueExt.class);

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = AdvancedCFController.SERVICE)
    private AdvancedCFController advancedCFController;

    @JIRAResource
    private EventPublisher eventPublisher;

    @JIRAResource
    private IssueService issueService;

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WorklogService worklogService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final CustomFieldManager customFieldManager;
    private final SubTaskManager subTaskManager;

    public IssueExt(JiraAuthenticationContext jiraAuthenticationContext, WorklogService worklogService,
                    VelocityRequestContextFactory velocityRequestContextFactory, CustomFieldManager customFieldManager, SubTaskManager subTaskManager) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.worklogService = worklogService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.customFieldManager = customFieldManager;
        this.subTaskManager = subTaskManager;
    }

    // Test method for PoC.
    public Response createIssue(IssueCreateRequest createRequest, @Context UriInfo uriInfo) {
        IssueFields issueFields = createRequest.fields();
        String rid = createRequest.getRid();
        ResourceId parentId = issueFields.parent();

        IssueInputParameters issueInput = createInputParameters(issueFields, rid);
        IssueService.CreateValidationResult validationResult = doValidate(issueInput, parentId);
        if (!validationResult.isValid()) {
            throw error(BAD_REQUEST, ErrorCollection.of(validationResult.getErrorCollection()));
        }

        IssueService.IssueResult creationResult = doCreate(validationResult, parentId);
        if (!creationResult.isValid()) {
            throw error(BAD_REQUEST, ErrorCollection.of(creationResult.getErrorCollection()));
        }

        /**
         * Issue creation worked
         **/
        // Clear any temp values
        advancedCFController.clearAdvancedCF(callingUser());
        LogWork logWork = issueFields.logWork();
        Issue issue = creationResult.getIssue();
        if (logWork != null) {
            tryLogWork(issue, logWork);
        }

        IssueCreateResponse response = new IssueCreateResponse()
                .id(issue.getId().toString())
                .key(issue.getKey())
                .self(createSelfLink(issue))
                .iconPath(excaliburWebUtil.getFullIconUrl(issue));

        eventPublisher.publish(new RestCreateIssueEvent(jiraAuthenticationContext.getUser(), issue));
        return status(CREATED).entity(response).build();
    }

    protected void tryLogWork(Issue issue, LogWork logWork) {
        WorklogResult worklogValidation = null;
        try {
            worklogValidation = worklogService.validateCreate(new JiraServiceContextImpl(callingUser()), WorklogInputParametersImpl.builder()
                            .issue(issue)
                            .startDate(logWork.started() != null ? parseLogWorkDate(logWork.started()) : new Date())
                            .timeSpent(logWork.timeSpent()).build()
            );
        } catch (ParseException e) {
            // ignore
        }

        if (worklogValidation == null) {
            throw error(BAD_REQUEST, ErrorCollection.of("Worklog did not pass validation"));
        }

        Worklog createdWorklog = worklogService.createAndRetainRemainingEstimate(new JiraServiceContextImpl(callingUser()), worklogValidation, true);
        if (createdWorklog == null) {
            throw error(BAD_REQUEST, ErrorCollection.of("Worklog was not created"));
        }
    }

    /*
     * This method is changed in the jira50 branch so be conscious of it that's all
     */
    private Date parseLogWorkDate(final String workStartedStr) throws ParseException {
        // here to encapsulate a deprecation in jira5.x
        return jiraAuthenticationContext.getOutlookDate().parseDateTimePicker(workStartedStr);
    }

    protected WebApplicationException error(Response.Status status, ErrorCollection errors) {
        return new WebApplicationException(Response.status(status).entity(errors).cacheControl(never()).build());
    }

    protected ApplicationUser callingUser() {
        return jiraAuthenticationContext.getUser();
    }

    protected IssueInputParameters createInputParameters(IssueFields fields, String rid) {
        IssueInputParametersExt parameters = new IssueInputParametersExt();
        parameters.setProjectId((fields.project() != null && fields.project().id() != null) ? Long.valueOf(fields.project().id()) : null);
        parameters.setSummary(fields.summary());
        parameters.setIssueTypeId(fields.issueType() != null ? fields.issueType().id() : null);
        parameters.setAssigneeId(fields.assignee() != null ? fields.assignee().id() : null);
        parameters.setReporterId(fields.reporter() != null ? fields.reporter().id() : null);
        parameters.setPriorityId(fields.priority() != null ? fields.priority().id() : null);
        parameters.setLabels(fields.labels());
        parameters.setTimeTracking(fields.timetracking());
        parameters.setSecurityLevelId(fields.security() != null ? Long.valueOf(fields.security().id()) : null);
        parameters.setAffectedVersionIds(asArray(fields.versions()));
        parameters.setEnvironment(fields.environment());
        parameters.setDescription(fields.description());
        parameters.setDueDate(fields.duedate());
        parameters.setFixVersionIds(asArray(fields.fixVersions()));
        parameters.setComponentIds(asArray(fields.components()));
        parameters.setIssueLinks(fields.getIssuelinks());
        for (Map.Entry<Long, String[]> customFieldEntry : fields.customFields().entrySet()) {
            Long customFieldId = customFieldEntry.getKey();
            String[] customFieldValues = customFieldEntry.getValue();

            CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
            if (customField == null) {
                // BON-685 - Not sure how this can happen.  We had this happen on qa-eacj but when we
                // we went back to replicate it, it would NOT happen again.  So IF it does happen
                // we skip the bad custom field and continue on
                log.error("Cannot find custom field '" + customFieldId + "' that was specified in Bonfire Create Issue request");
                continue;
            }
            if (customFieldRequiresKeys(customField)) {
                String cfStringId = customField.getId();

                for (int i = 0; i < customFieldValues.length; i++) {
                    // some custom fields require this quirky format, e.g. "customfield_10000:1"
                    String cfFullKey = i == 0 ? cfStringId : String.format("%s:%d", cfStringId, i);
                    parameters.addCustomFieldValue(cfFullKey, customFieldValues[i]);
                }
            } else {
                parameters.addCustomFieldValue(customFieldId, customFieldValues);
            }
        }
        // Now add advanced custom fields
        Map<String, String[]> advancedFields = getAdvancedCFAsMap(parameters.getProjectId(), parameters.getIssueTypeId(), rid);
        if (advancedFields != null) {
            for (String s : advancedFields.keySet()) {
                CustomField customField = customFieldManager.getCustomFieldObject(s);
                if (customField != null) {
                    parameters.addCustomFieldValue(customField.getIdAsLong(), advancedFields.get(s));
                }
            }
        }

        return parameters;
    }

    protected String createSelfLink(Issue issue) {
        String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();

        return UriBuilder.fromUri(baseUrl).path("/rest/api/latest/issue/{issueKey}").build(issue.getKey()).toString();
    }

    protected boolean customFieldRequiresKeys(CustomField customField) {
        return customField.getCustomFieldType() instanceof CascadingSelectCFType;
    }

    private Map<String, String[]> getAdvancedCFAsMap(Long pid, String itid, String rid) {
        try {
            Long issueTypeId = Long.valueOf(itid);
            return advancedCFController.getAdvancedCFAsMap(callingUser(), pid, issueTypeId, rid);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private IssueService.CreateValidationResult doValidate(IssueInputParameters issueInput, ResourceId parentId) {
        if (parentId != null) {
            return issueService.validateSubTaskCreate(callingUser(), Long.valueOf(parentId.id()), issueInput);
        } else {
            return issueService.validateCreate(callingUser(), issueInput);
        }
    }

    private IssueService.IssueResult doCreate(IssueService.CreateValidationResult validationResult, ResourceId parentId) {
        IssueService.IssueResult issueCreation = issueService.create(callingUser(), validationResult);

        if (parentId == null || !issueCreation.isValid()) {
            // Return if not subtask or if there is an error
            return issueCreation;
        } else {
            // Check the parent is valid
            IssueService.IssueResult parentIssueResult = issueService.getIssue(callingUser(), Long.valueOf(parentId.id()));
            if (!parentIssueResult.isValid()) {
                return parentIssueResult;
            }

            // so far so good. now create the issue link.
            try {
                subTaskManager.createSubTaskIssueLink(parentIssueResult.getIssue(), issueCreation.getIssue(), callingUser());

                // return the created issue
                return issueCreation;
            } catch (CreateException e) {
                com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
                errors.addErrorMessage(jiraAuthenticationContext.getI18nHelper().getText("admin.errors.project.import.issue.link.error"));

                return new IssueService.IssueResult(issueCreation.getIssue(), errors);
            }
        }
    }

    private static Long[] asArray(List<ResourceId> ids) {
        if (ids == null) {
            return null;
        }

        Long[] longs = new Long[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            longs[i] = Long.valueOf(ids.get(i).id());
        }

        return longs;
    }
}
