package com.thed.zephyr.capture.service.parser;

import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

import javax.annotation.Nullable;

/**
 * <p>
 * Handles parsing custom fields from rendered html
 * </p>
 * Related issues:
 * <ul>
 * <li>BONDEV-348 (JDOG)</li>
 * <li>BON-2056 (JAC)</li>
 * </ul>
 *
 * @since v2.9.1
 */
public interface FieldDetailsParser {
    static final String SERVICE = "capture-fieldDetailsParser";

    /**
     * Renders field using getCreateHtml and attempts to parse field details from the generated html
     *
     * @param project                     project
     * @param issueType                   issue type
     * @param fieldScreenRenderLayoutItem field screen layout item
     * @return parsed field details or null, if can not be parsed
     */
    @Nullable
    ParsedField parseFieldDetails(Project project, IssueType issueType, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem);
}
