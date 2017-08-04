package com.atlassian.bonfire.service.parser;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.web.action.issue.CreateIssue;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles parsing custom fields from rendered html
 *
 * @since v2.9.1
 */
@Service(FieldDetailsParser.SERVICE)
public class FieldDetailsParserImpl implements FieldDetailsParser {
    private static final Logger log = Logger.getLogger(FieldDetailsParserImpl.class);

    @JIRAResource
    private IssueFactory jiraIssueFactory;

    @JIRAResource
    private IssueCreationHelperBean issueCreationHelperBean;

    @JIRAResource
    private ProjectFactory projectFactory;

    @Override
    public ParsedField parseFieldDetails(Project project, IssueType issueType, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        final CreateIssue createBean = new CreateIssue(jiraIssueFactory, issueCreationHelperBean);
        final String html = fieldScreenRenderLayoutItem.getCreateHtml(createBean, createBean, getIssueObject(project, issueType), getDisplayParams());
        if (StringUtils.isEmpty(html)) {
            // nothing to parse
            return null;
        }
        final Document doc = Jsoup.parseBodyFragment(html);
        final Element body = doc.body();

        // Currently we support only custom field rendered as select element to resolve BON-2056,
        // further custom field to be supported based on requests
        final ParsedField parsedField = parseSelectElement(body, fieldScreenRenderLayoutItem);
        if (parsedField != null) {
            return parsedField;
        }
        return null;
    }

    /**
     * Parses select element from a document
     *
     * @param body                        html element to parse select
     * @param fieldScreenRenderLayoutItem field screen layout
     * @return parsed field or null if can not be parsed properly
     */
    private ParsedField parseSelectElement(Element body, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem) {
        final OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

        final Elements allSelectElements = body.getElementsByTag("select");

        final List<Element> filteredSelect = Lists.newArrayList(Iterables.filter(allSelectElements, new Predicate<Element>() {
            @Override
            public boolean apply(Element element) {
                return element != null && element.id() != null && element.id().equals(orderableField.getId());
            }
        }));

        // NOTE: currently supporting only single select rendered in html
        if (filteredSelect.size() != 1) {
            return null;
        }
        final Iterable<Element> options = getOptions(filteredSelect.get(0));
        if (Iterables.isEmpty(options)) {
            return null;
        }

        final Iterable<ParsedFieldOption> optionsDtoIterable = Iterables.transform(options, new Function<Element, ParsedFieldOption>() {
            @Override
            public ParsedFieldOption apply(Element element) {
                return new ParsedFieldOption(element.text(), element.val());
            }
        });
        if (Iterables.isEmpty(optionsDtoIterable)) {
            return null;
        }
        final String description = fieldScreenRenderLayoutItem.getFieldLayoutItem().getFieldDescription();
        return new ParsedField(orderableField.getId(), orderableField.getName(), ParsedFieldType.SINGLE_SELECT.getTypeKey(), description, ParsedFieldType.SINGLE_SELECT, Lists.newArrayList(optionsDtoIterable));
    }

    /**
     * Parses options from the select element. Options from the optgroup elements are added too.
     *
     * @param select select element to parse options
     * @return options iterable
     */
    private Iterable<Element> getOptions(Element select) {
        final Elements optionsAndOptGroups = select.children();

        final List<Element> optGroups = new ArrayList<Element>();
        optGroups.addAll(optionsAndOptGroups);

        for (Element element : optionsAndOptGroups) {
            if (StringUtils.equalsIgnoreCase("optgroup", element.tagName())) {
                optGroups.addAll(element.children());
            }
        }

        final Iterable<Element> filteredOptions = Iterables.filter(optGroups, new Predicate<Element>() {
            @Override
            public boolean apply(Element element) {
                // empty value is added by a default on a front-end part in fields.js
                return element != null && StringUtils.equalsIgnoreCase("option", element.tagName()) && !StringUtils.isEmpty(element.val());
            }
        });

        return filteredOptions;
    }

    /*
     * The issue that gets returned here is simply a wrapper for the project and projectId. JIRA does it too.
     */
    public MutableIssue getIssueObject(Project p, IssueType t) {
        final MutableIssue issueObject = jiraIssueFactory.getIssue();
        issueObject.setProjectId(p.getId());
        issueObject.setIssueTypeId(t.getId());

        return issueObject;
    }

    /**
     * Display parameters that are needed to properly render some fields
     */
    private Map<String, Object> getDisplayParams() {
        final Map<String, Object> displayParams = Maps.newHashMap();
        displayParams.put("theme", "aui");
        return displayParams;
    }

    @VisibleForTesting
    public void setJiraIssueFactory(IssueFactory jiraIssueFactory) {
        this.jiraIssueFactory = jiraIssueFactory;
    }

}
