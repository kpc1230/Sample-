package com.atlassian.bonfire;


import com.atlassian.bonfire.util.PageObjectsInjectorCopy;
import com.atlassian.bonfire.util.RestoreJiraFromBackupCopy;
import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.junit.runners.model.InitializationError;

/**
 * One level of abstraction incase we need it - partially copied from GH
 *
 * @author ezhang
 */
public class CaptureTestRunner extends CompositeTestRunner {
    public static Composer jiraComposer(JiraTestedProduct product) {
        return compose().from(RestoreJiraFromBackupCopy.compose(product)).from(PageObjectsInjectorCopy.compose(product));
    }

    public CaptureTestRunner(Class<?> cls) throws InitializationError {
        super(cls, jiraComposer(buildTestedProduct()));
    }

    /**
     * Set the system property for the Browser version, if not previously done. Working around the deficiencies of JUnit, this is useful for local
     * test runs. CI builds will set the property beforehand so this won't do anything in that case.
     */
    private static JiraTestedProduct buildTestedProduct() {
        // yeah, it's all constructor chained
        return TestedProductFactory.create(JiraTestedProduct.class);
    }
}
