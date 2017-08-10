package com.thed.zephyr.capture.customfield.searcher;

import java.util.Set;
import javax.annotation.Nonnull;
import com.querydsl.core.types.Order;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.dbquery.ClauseDbQueryFactory;
import com.atlassian.jira.jql.dbquery.ClauseFactoryContext;
import com.atlassian.jira.jql.dbquery.DbNameResolver;
import com.atlassian.jira.jql.dbquery.DbNameResolvers;
import com.atlassian.jira.jql.dbquery.GroupByClause;
import com.atlassian.jira.jql.dbquery.JqlPredicate;
import com.atlassian.jira.jql.dbquery.OrderByClause;
import com.atlassian.jira.jql.dbquery.SelectClause;
import com.atlassian.jira.jql.dbquery.WhereClause;
import com.atlassian.query.ClauseType;

import com.thed.zephyr.capture.customfield.BonfireSessionCFType;

import static com.atlassian.jira.jql.dbquery.SimpleStringResolver.sameCaseResolver;

public class SessionClauseDbQueryFactory implements ClauseDbQueryFactory {
    final CustomField customField;

    public SessionClauseDbQueryFactory(CustomField customField) {
        this.customField = customField;
    }

    public DbNameResolver<String> getStringDbNameResolver() {
        return  DbNameResolvers.adaptForStringValues(sameCaseResolver());
    }

    @Override
    public WhereClause buildWhereClause(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull JqlPredicate jqlPredicate) {
        if (customField.getCustomFieldType().getClass().equals(BonfireSessionCFType.class)) {
            return new RaisedDuringClauseDbQueryFactory(customField)
                    .buildWhereClause(clauseFactoryContext, jqlPredicate);
        } else {
            return new MultiSessionClauseDbQueryFactory(customField)
                    .buildWhereClause(clauseFactoryContext, jqlPredicate);
        }
    }

    @Override
    public OrderByClause buildOrderBy(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull Order sortOrder) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SelectClause buildSelect(@Nonnull ClauseFactoryContext clauseFactoryContext) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public GroupByClause buildGroupBy(@Nonnull ClauseFactoryContext clauseFactoryContext) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<ClauseType> getSupportedClauseTypes() {
        return ClauseType.WHERE_ONLY;
    }
}
