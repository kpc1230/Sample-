package com.thed.zephyr.capture.customfield.searcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.atlassian.jira.jql.dbquery.DbClauseBuilderFactory;
import com.atlassian.jira.jql.dbquery.DbOrderByClauseBuilder;
import com.atlassian.jira.jql.dbquery.DbWhereClauseBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLQuery;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.dbquery.ClauseDbQueryFactory;
import com.atlassian.jira.jql.dbquery.ClauseFactoryContext;
import com.atlassian.jira.jql.dbquery.DbNameResolver;
import com.atlassian.jira.jql.dbquery.GroupByClause;
import com.atlassian.jira.jql.dbquery.JqlPredicate;
import com.atlassian.jira.jql.dbquery.OrderByClause;
import com.atlassian.jira.jql.dbquery.SelectClause;
import com.atlassian.jira.jql.dbquery.WhereClause;
import com.atlassian.jira.jql.dbquery.WhereClauseBuilder;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.query.ClauseType;

import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.TestingStatusService.TestingStatus;

import static com.atlassian.jira.jql.dbquery.SimpleStringResolver.sameCaseResolver;
import static com.atlassian.jira.model.querydsl.QCustomFieldValue.CUSTOM_FIELD_VALUE;
import static com.atlassian.jira.model.querydsl.QIssue.ISSUE;

public class TestingStatusClauseDbQueryFactory implements ClauseDbQueryFactory {

    private static final String TESTING_STATUS_FIELD_NAME = "TestingStatus";
    private final CustomField customField;
    private final BonfireI18nService i18n;
    private final String i18nNotStartedKey;
    private final DbOrderByClauseBuilder dbOrderByClauseBuilder;
    private final DbWhereClauseBuilder dbWhereClauseBuilder;

    public TestingStatusClauseDbQueryFactory(CustomField customField, BonfireI18nService i18nService, DbClauseBuilderFactory dbClauseBuilderFactory) {
        this.i18n = i18nService;
        this.customField = customField;
        this.dbOrderByClauseBuilder = dbClauseBuilderFactory.orderByForCustomFieldValueTable(customField, CUSTOM_FIELD_VALUE.stringvalue);
        this.dbWhereClauseBuilder = dbClauseBuilderFactory.createWhereClauseBuilder(Expressions.path(String.class, TESTING_STATUS_FIELD_NAME), getStringDbNameResolver(), valuePredicate -> getWhereClause(valuePredicate));
        i18nNotStartedKey = TestingStatus.NOT_STARTED.getI18nKey();
    }

    private DbNameResolver<String> getStringDbNameResolver() {
        return  adaptForStringValues(sameCaseResolver());
    }

    public DbNameResolver<String> adaptForStringValues(NameResolver<?> nameResolver) {
        Map<String, String> testingStatusMap = getMapWithCurrentLocale();
        return rawValues -> {
            Collection<String> names = rawValues.stream()
                    .map(q -> testingStatusMap.get(q.asString()))
                    .collect(Collectors.toSet());
            return nameResolver.getIdsFromNames(names).values();
        };
    }

    @Override
    public WhereClause buildWhereClause(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull JqlPredicate jqlPredicate) {
        return dbWhereClauseBuilder.buildWhereClause(clauseFactoryContext, jqlPredicate);
    }

    private SQLQuery<Long> getWhereClause(BooleanExpression valuePredicate) {

        SQLQuery<Tuple> allIssuesWithTestingStatus = new SQLQuery<>()
                .select(ISSUE.id.as("issue"), new CaseBuilder()
                        .when(CUSTOM_FIELD_VALUE.stringvalue.isNotNull())
                        .then(CUSTOM_FIELD_VALUE.stringvalue)
                        .otherwise(i18nNotStartedKey).as("TestingStatus"))
                .from(ISSUE)
                .leftJoin(CUSTOM_FIELD_VALUE)
                .on(ISSUE.id.eq(CUSTOM_FIELD_VALUE.issue).and(CUSTOM_FIELD_VALUE.customfield.eq(customField.getIdAsLong())));

        return new SQLQuery<>(PostgreSQLTemplates.builder().quote().build())
                .select(Expressions.path(Long.class, "issue"))
                .from(allIssuesWithTestingStatus.as("testStatus"))
                .where(valuePredicate);
    }


    @Override
    public OrderByClause buildOrderBy(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull Order sortOrder) {
        return dbOrderByClauseBuilder.buildOrderByClause(clauseFactoryContext, sortOrder);
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
        return ClauseType.ORDER_BY_AND_WHERE;
    }

    private Map<String, String> getMapWithCurrentLocale() {
        Map<String, String> testingStatusMap =  new HashMap<>();

        testingStatusMap.put(i18n.getText(TestingStatus.NOT_STARTED.getI18nKey()), TestingStatus.NOT_STARTED.getI18nKey());
        testingStatusMap.put(i18n.getText(TestingStatus.IN_PROGRESS.getI18nKey()), TestingStatus.IN_PROGRESS.getI18nKey());
        testingStatusMap.put(i18n.getText(TestingStatus.COMPLETED.getI18nKey()), TestingStatus.COMPLETED.getI18nKey());
        testingStatusMap.put(i18n.getText(TestingStatus.INCOMPLETE.getI18nKey()), TestingStatus.INCOMPLETE.getI18nKey());

        return testingStatusMap;
    }
}
