package com.thed.zephyr.capture.customfield.searcher;

import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.util.BonfireServiceAccessor;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldValueGeneratingClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.dbquery.DbClauseBuilderFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ActualValueCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class TestingStatusSearcher extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher {
    private final FieldVisibilityManager fieldVisibilityManager;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final JiraFeatureFlagService jiraFeatureFlagService;
    private final DbClauseBuilderFactory dbClauseBuilderFactory;

    private final JqlOperandResolver jqlOperandResolver;
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    public TestingStatusSearcher(FieldVisibilityManager fieldVisibilityManager, CustomFieldInputHelper customFieldInputHelper,
                                 JiraFeatureFlagService jiraFeatureFlagService, DbClauseBuilderFactory dbClauseBuilderFactory,
                                 JqlOperandResolver jqlOperandResolver) {
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.jiraFeatureFlagService = jiraFeatureFlagService;
        this.dbClauseBuilderFactory = dbClauseBuilderFactory;
        this.jqlOperandResolver = jqlOperandResolver;
    }

    @Override
    public void init(CustomField field) {
        BonfireI18nService i18n = BonfireServiceAccessor.getInstance().getI18n();

        ClauseNames names = field.getClauseNames();
        TestingStatusValueConverter indexValueConverter = new TestingStatusValueConverter(i18n);

        // custom field indexer - Turns the value on the issue into something to match against the search
        FieldIndexer indexer = new TestingStatusCustomFieldIndexer(fieldVisibilityManager, field, jiraFeatureFlagService);

        CustomFieldValueProvider customFieldValueProvider = new SingleValueCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(),
                Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));

        this.searchRenderer = new EmptySearchRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new NoSimpleSearchInputTransformer(field, names, searcherInformation.getId(), customFieldInputHelper);

        ClauseValidator validator = new TestingStatusClauseValidator();
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(validator,
                new ActualValueCustomFieldClauseQueryFactory(field.getId(), jqlOperandResolver, indexValueConverter, true),
                new TestingStatusClauseDbQueryFactory(field, i18n, dbClauseBuilderFactory),
                new TestingStatusClauseValuesGenerator(i18n), OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.TEXT);
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
