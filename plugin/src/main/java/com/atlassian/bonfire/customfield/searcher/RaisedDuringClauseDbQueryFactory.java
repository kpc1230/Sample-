package com.atlassian.bonfire.customfield.searcher;

import javax.annotation.Nonnull;
import com.querydsl.sql.SQLQuery;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.dbquery.ClauseFactoryContext;
import com.atlassian.jira.jql.dbquery.JqlPredicate;
import com.atlassian.jira.jql.dbquery.WhereClause;
import com.atlassian.jira.jql.dbquery.WhereClauseBuilder;

import static com.atlassian.jira.model.querydsl.QCustomFieldValue.CUSTOM_FIELD_VALUE;
import static com.atlassian.jira.model.querydsl.QIssue.ISSUE;

public class RaisedDuringClauseDbQueryFactory extends SessionClauseDbQueryFactory {


    public RaisedDuringClauseDbQueryFactory(CustomField customField) {
        super(customField);
    }

    @Override
    public WhereClause buildWhereClause(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull JqlPredicate jqlPredicate) {
        return new WhereClauseBuilder(CUSTOM_FIELD_VALUE.stringvalue,
                getStringDbNameResolver(),
                valuePredicate -> new SQLQuery<>()
                        .select(ISSUE.id)
                        .from(ISSUE)
                        .leftJoin(CUSTOM_FIELD_VALUE)
                        .on(ISSUE.id.eq(CUSTOM_FIELD_VALUE.issue).and(CUSTOM_FIELD_VALUE.customfield.eq(customField.getIdAsLong())))
                        .where(valuePredicate))
                .buildWhereClause(clauseFactoryContext, jqlPredicate);
    }
}
