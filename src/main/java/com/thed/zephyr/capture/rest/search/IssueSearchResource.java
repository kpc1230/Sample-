package com.thed.zephyr.capture.rest.search;

import com.thed.zephyr.capture.rest.model.AutocompleteBean;
import com.thed.zephyr.capture.rest.model.AutocompleteBeans;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.core.util.bean.PagerFilter;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.issue.search.IssuePickerResults;
import com.atlassian.jira.bc.issue.search.IssuePickerSearchService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.Callable;

@Path("issueSearch")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class IssueSearchResource extends BonfireRestResource {
    private static final int MAX_ISSUES_TO_SEND = 20;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService bonfireI18nService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @JIRAResource
    private IssuePickerSearchService jiraIssuePickerSearchService;

    @JIRAResource
    private SearchService jiraSearchService;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    public IssueSearchResource() {
        super(IssueSearchResource.class);
    }

    @GET
    @Path("/autocomplete")
    public Response searchIssues(final @QueryParam("term") String term) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                Collection<IssuePickerResults> pickerResults = buildAndExecuteIssuePickerSearch(term);
                List<AutocompleteBean> beans = getBeansFromResults(pickerResults);

                return ok(new AutocompleteBeans(beans));
            }
        });
    }

    @GET
    @Path("/autocompleteEpic")
    public Response searchEpics(final @QueryParam("term") String term) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                SearchResults epicResults = buildAndExecuteEpicPickerSearch(term);
                List<AutocompleteBean> beans = getBeansFromSearchResults(epicResults);

                return ok(new AutocompleteBeans(beans));
            }
        });
    }

    @GET
    @Path("/autocomplete/{projectKey}")
    public Response searchIssuesByProject(final @QueryParam("term") String term, final @PathParam("projectKey") String projectKey) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                Collection<IssuePickerResults> pickerResults = buildAndExecuteIssuePickerSearch(term, projectKey);
                List<AutocompleteBean> beans = getBeansFromResults(pickerResults);

                return ok(new AutocompleteBeans(beans));
            }
        });
    }

    /**
     * Converts a list of issues into AutocompleteBeans.
     *
     * @param pickerResults - Results from the search
     * @return the results
     */
    private List<AutocompleteBean> getBeansFromResults(Collection<IssuePickerResults> pickerResults) {
        List<AutocompleteBean> beans = new ArrayList<AutocompleteBean>();
        Set<String> duplicatePrevention = new HashSet<String>();
        for (IssuePickerResults result : pickerResults) {
            Collection<Issue> issues = result.getIssues();
            for (Issue issue : issues) {
                if (!duplicatePrevention.contains(issue.getKey())) {
                    beans.add(buildAutoCompleteBean(issue));
                    duplicatePrevention.add(issue.getKey());
                }
            }
        }
        if (beans.isEmpty()) {
            beans.add(new AutocompleteBean("", bonfireI18nService.getText("rest.search.issues.none"), "", true));
        }
        return beans;
    }

    private List<AutocompleteBean> getBeansFromSearchResults(SearchResults results) {
        final List<AutocompleteBean> beans = Lists.transform(results.getIssues(), new Function<Issue, AutocompleteBean>() {
            @Override
            public AutocompleteBean apply(@Nullable final Issue issue) {
                return buildAutoCompleteBean(issue);
            }
        });

        if (beans.isEmpty()) {
            return Lists.newArrayList(new AutocompleteBean("", bonfireI18nService.getText("rest.search.issues.none"), "", true));
        }
        return ImmutableList.copyOf(beans); // Serialization
    }

    private Collection<IssuePickerResults> buildAndExecuteIssuePickerSearch(String term) {
        String currentJQL = "ORDER BY updated DESC";
        IssuePickerSearchService.IssuePickerParameters pickerParameters = new IssuePickerSearchService.IssuePickerParameters(term, currentJQL, null,
                null, true, true, MAX_ISSUES_TO_SEND);
        return jiraIssuePickerSearchService.getResults(buildJiraServiceContext(), pickerParameters);
    }

    private SearchResults buildAndExecuteEpicPickerSearch(String term) throws SearchException {
        // JQL: (issuekey = "term" or summary ~ "term" or project = "term") and issuetype = epic order by updateDate desc
        final Query recentlyUpdatedEpicsQuery = JqlQueryBuilder.newBuilder().where()
                .sub() /* JQL: opening parentheses '(' */
                .issue(term)
                .or()
                .project(term)
                .or()
                .summary(term)
                .endsub() /* JQL: closing parentheses ')' */
                .and()
                .issueType("epic").endWhere()
                .orderBy().updatedDate(SortOrder.DESC).buildQuery();

        final PagerFilter pagerFilter = new PagerFilter();
        pagerFilter.setMax(MAX_ISSUES_TO_SEND);
        return jiraSearchService.search(jiraAuthenticationContext.getLoggedInUser(), recentlyUpdatedEpicsQuery, new com.atlassian.jira.web.bean.PagerFilter(MAX_ISSUES_TO_SEND));
    }

    private Collection<IssuePickerResults> buildAndExecuteIssuePickerSearch(String term, String projectKey) {
        Project project = jiraProjectManager.getProjectObjByKey(projectKey);
        String currentJQL = "project = " + projectKey + " ORDER BY updated DESC";
        IssuePickerSearchService.IssuePickerParameters pickerParameters = new IssuePickerSearchService.IssuePickerParameters(term, currentJQL, null,
                project, true, true, MAX_ISSUES_TO_SEND);
        return jiraIssuePickerSearchService.getResults(buildJiraServiceContext(), pickerParameters);
    }

    /**
     * Includes the issuetype icon. Used for plugin pages.
     */
    private AutocompleteBean buildAutoCompleteBean(Issue i) {
        String fullImgSrc = excaliburWebUtil.getFullIconUrl(i);
        String label = i.getKey() + " - " + i.getSummary();
        return new AutocompleteBean(i.getId().toString(), label, i.getKey(), fullImgSrc);
    }
}
