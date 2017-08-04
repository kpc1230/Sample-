package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractSingleValueCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.Query;

/**
 * Transforms between advance and simple search. In this case we want to deny that transformation if our clause exists. Adapted from GreenHopper code
 */
public class
        NoSimpleSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer {
    public NoSimpleSearchInputTransformer(CustomField field, ClauseNames clauseNames, String urlParameterName,
                                          CustomFieldInputHelper customFieldInputHelper) {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
    }

    /**
     * Tells the caller whether or not the relevant clauses from the passed query can be represented on the issue
     * navigator.
     *
     * @param searcher      searcher
     * @param query         query
     * @param searchContext search context
     * @return true if the query can be represented on navigator.
     * @deprecated Use {@link NoSimpleSearchInputTransformer#doRelevantClausesFitFilterForm(com.atlassian.jira.user.ApplicationUser, com.atlassian.query.Query, com.atlassian.jira.issue.search.SearchContext)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public boolean doRelevantClausesFitFilterForm(User searcher, Query query, SearchContext searchContext) {
        return false;
    }

    public boolean doRelevantClausesFitFilterForm(ApplicationUser applicationUser, Query query, SearchContext searchContext) {
        return false;
    }
}
