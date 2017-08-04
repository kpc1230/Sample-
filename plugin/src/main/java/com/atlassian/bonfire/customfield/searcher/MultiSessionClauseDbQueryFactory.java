package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.dbquery.ClauseFactoryContext;
import com.atlassian.jira.jql.dbquery.JqlPredicate;
import com.atlassian.jira.jql.dbquery.WhereClause;
import com.atlassian.jira.jql.dbquery.WhereClauseBuilder;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.model.querydsl.QCustomFieldValue;
import com.atlassian.query.operator.Operator;
import com.opensymphony.util.TextUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLQuery;

import javax.annotation.Nonnull;
import java.util.List;

import static com.atlassian.jira.model.querydsl.QIssue.ISSUE;

public class MultiSessionClauseDbQueryFactory extends SessionClauseDbQueryFactory {

    /**
     * TestSessions related to issue are stored as string ('10015, 10016, ')
     * This SQL checks if array passed as string (parameter {1}) has any elements in common with array of related TestSessions
     */
    private static final String COMPARE_ARRAYS = "(string_to_array({1}, ', ') && (array_remove(string_to_array( {0} , ', ', ''), NULL)))";

    public MultiSessionClauseDbQueryFactory(CustomField customField) {
        super(customField);
    }

    @Override
    public WhereClause buildWhereClause(@Nonnull ClauseFactoryContext clauseFactoryContext, @Nonnull JqlPredicate jqlPredicate) {
        if((jqlPredicate.isEmptyOperand() || jqlPredicate.getOperandValues().containsEmpty())){
            return getWhereClauseForEmptyOperands(clauseFactoryContext, jqlPredicate);
        } else {
            return createWhereClauseForOtherOperands(clauseFactoryContext, jqlPredicate);
        }
    }

    private WhereClause getWhereClauseForEmptyOperands(ClauseFactoryContext clauseFactoryContext, JqlPredicate jqlPredicate) {
        return new WhereClauseBuilder(QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue,
                getStringDbNameResolver(),
                (valuePredicate, context, jqlPred) -> createWhereClauseForEmptyOperand(jqlPredicate, valuePredicate, context))
                .buildWhereClause(clauseFactoryContext, jqlPredicate);
    }

    private SQLQuery<Long> createWhereClauseForEmptyOperand(final JqlPredicate jqlPredicate,
                                                            final BooleanExpression valuePredicate,
                                                            final ClauseFactoryContext context) {
        final BooleanExpression whereExpression;

        // The new where clause builder does double inversions (where looks up the values you don't want, then inverts
        // that to get the value that you do want) - and sometimes does it again depending on whether you wanted or didn't
        // want those values. It sounds somewhat complicated, and it kinda is, but it makes a massive difference performance
        // wise. This will happen in cases where there's "foo in (X,Y,EMPTY)" or "foo not in (X,Y)".
        // If this does happen, it'll signal that this has occurred by wrapping the ClauseFactoryContext in a DoubleInversionClauseFactoryContext
        // class. This can be used to subtly trigger the change in behaviour for some select where clause builders, but should work for
        // all clause builders that don't re-write their where clauses and use .
        // If a this happens, just trust the where expression and don't re-write it.
        if (context.getClass().toString().contains("DoubleInversionClauseFactoryContext")) {
            whereExpression = valuePredicate;
        } else {
            if (jqlPredicate.isEmptyOperand() ||
                    (jqlPredicate.getOperandValues().containsEmpty() && jqlPredicate.getOperandValues().getNonEmptyValues().isEmpty())) {
                whereExpression = valuePredicate;
            } else {
                String joinedArguments = TextUtils.join(", ", jqlPredicate.getRawValues());
                whereExpression = Expressions.booleanTemplate(COMPARE_ARRAYS, QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue, joinedArguments)
                        .or(QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue.isNull());
            }
        }

        return new SQLQuery<>()
                .select(ISSUE.id)
                .from(ISSUE)
                .leftJoin(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                .on(ISSUE.id.eq(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                        .and(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield.eq(customField.getIdAsLong())))
                .where(whereExpression);
    }

    private WhereClause createWhereClauseForOtherOperands(ClauseFactoryContext clauseFactoryContext, JqlPredicate jqlPredicate) {
        return new WhereClauseBuilder(QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue,
                getStringDbNameResolver(),
                (valuePredicate, context, jqlPred) -> getSQLQueryForOtherOperands(jqlPredicate, valuePredicate, context))
                .buildWhereClause(clauseFactoryContext, jqlPredicate);
    }

    private SQLQuery<Long> getSQLQueryForOtherOperands(final JqlPredicate jqlPredicate,
                                                       final BooleanExpression valuePredicate,
                                                       final ClauseFactoryContext context) {
        BooleanExpression whereExpression;
        // Giant essay about why this has to exist above.
        if (context.getClass().toString().contains("DoubleInversionClauseFactoryContext")) {
            whereExpression = valuePredicate;
        } else {
            Operator operator = jqlPredicate.getOperator();
            List<QueryLiteral> arguments = jqlPredicate.getRawValues();

            String joinedArguments = TextUtils.join(", ", arguments);

            whereExpression = Expressions.booleanTemplate(COMPARE_ARRAYS, QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue, joinedArguments);

            if (operator == Operator.NOT_IN || operator == Operator.NOT_EQUALS) {
                whereExpression = whereExpression.or(QCustomFieldValue.CUSTOM_FIELD_VALUE.textvalue.isNull());
            }
        }

        return new SQLQuery<>()
                .select(ISSUE.id)
                .from(ISSUE)
                .leftJoin(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                .on(ISSUE.id.eq(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                        .and(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield.eq(customField.getIdAsLong())))
                .where(whereExpression);
    }
}
