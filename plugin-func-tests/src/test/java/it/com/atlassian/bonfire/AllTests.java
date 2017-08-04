package it.com.atlassian.bonfire;

import it.com.atlassian.bonfire.rest.TestFieldsResource;
import junit.framework.TestSuite;

/**
 * Test suite setup
 *
 * @since v4.4
 */
public class AllTests {
    public static TestSuite suite() {
        final TestSuite testSuite = new TestSuite("Integration tests for Bonfire");

        testSuite.addTestSuite(TestFieldsResource.class);

        return testSuite;
    }
}
