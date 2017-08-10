package com.thed.zephyr.capture.customfield.searcher;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class SessionSearcher extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final CustomFieldInputHelper customFieldInputHelper;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    private static final JiraDataType SESSION = new JiraDataTypeImpl(Session.class);

    public SessionSearcher(FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver, DoubleConverter doubleConverter,
                           CustomFieldInputHelper customFieldInputHelper) {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    @Override
    public void init(CustomField field) {
        ClauseNames names = field.getClauseNames();
        SessionIndexValueConverter indexValueConverter = new SessionIndexValueConverter();

        // custom field indexer - Turns the value on the issue into something to match against the search
        FieldIndexer indexer = new SessionCustomFieldIndexer(fieldVisibilityManager, field);

        CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(),
                Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));

        this.searchRenderer = new EmptySearchRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new NoSimpleSearchInputTransformer(field, names, searcherInformation.getId(), customFieldInputHelper);

        ClauseValidator validator = new SessionClauseValidator(jqlOperandResolver);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(validator,
                new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, true),
                new SessionClauseDbQueryFactory(field),
                OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, SESSION);
    }

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        if (searchInputTransformer == null) {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer() {
        if (searchRenderer == null) {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (customFieldSearcherClauseHandler == null) {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }
}
