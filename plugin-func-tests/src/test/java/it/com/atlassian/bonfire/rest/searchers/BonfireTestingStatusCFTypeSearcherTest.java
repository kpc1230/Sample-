package it.com.atlassian.bonfire.rest.searchers;

import it.com.atlassian.bonfire.util.AbstractCustomFieldSearcherTest;

import java.util.stream.Stream;

public class BonfireTestingStatusCFTypeSearcherTest extends AbstractCustomFieldSearcherTest {

    private static final String[] NOT_STARTED_WITH_RELATED_TEST_SESSION_ISSUES = {"HSP-15", "HSP-14", "HSP-12", "HSP-11"};
    private static final String[] EMPTY_TESTING_STATUS_ISSUES = {"HSP-13", "HSP-10", "HSP-9", "HSP-8"};
    private static final String[] NOT_STARTED_ISSUES = unionStringArrays(NOT_STARTED_WITH_RELATED_TEST_SESSION_ISSUES, EMPTY_TESTING_STATUS_ISSUES);
    private static final String[] IN_PROGRESS_ISSUES = {"HSP-7", "HSP-6", "HSP-5", "HSP-4"};
    private static final String[] IN_COMPLETE_ISSUES = {"HSP-3"};
    private static final String[] COMPLETE_ISSUES = {"HSP-2", "HSP-1"};
    
    @Override
    public void setUpTest() {
        restoreData("capture-testingstatus-searcher.xml");
        runUpgradeTasks();
    }

    @Override
    public void testWhereClauses() {
        assertSearchContainsOnly("\"Testing status\" = \"Not started\"", NOT_STARTED_ISSUES);
        assertSearchContainsOnly("\"Testing status\" != \"Not started\"", unionStringArrays(IN_PROGRESS_ISSUES, IN_COMPLETE_ISSUES, COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" = \"In progress\"", IN_PROGRESS_ISSUES);
        assertSearchContainsOnly("\"Testing status\" != \"In progress\"", unionStringArrays(NOT_STARTED_ISSUES, IN_COMPLETE_ISSUES, COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" = \"Complete\"", COMPLETE_ISSUES);
        assertSearchContainsOnly("\"Testing status\" != \"Complete\"", unionStringArrays(NOT_STARTED_ISSUES, IN_PROGRESS_ISSUES, IN_COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" = \"Incomplete\"", IN_COMPLETE_ISSUES);
        assertSearchContainsOnly("\"Testing status\" != \"Incomplete\"", unionStringArrays(NOT_STARTED_ISSUES, IN_PROGRESS_ISSUES, COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" IN (\"Complete\")", COMPLETE_ISSUES);
        assertSearchContainsOnly("\"Testing status\" NOT IN (\"Complete\")", unionStringArrays(NOT_STARTED_ISSUES, IN_PROGRESS_ISSUES, IN_COMPLETE_ISSUES));

        assertSearchContainsNone("\"Testing status\" IN (EMPTY)");
        assertSearchContainsOnly("\"Testing status\" NOT IN (EMPTY)", unionStringArrays(NOT_STARTED_ISSUES, IN_PROGRESS_ISSUES, IN_COMPLETE_ISSUES, COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" IN (\"Complete\", EMPTY)", COMPLETE_ISSUES);
        assertSearchContainsOnly("\"Testing status\" NOT IN (\"Complete\", EMPTY)", unionStringArrays(NOT_STARTED_ISSUES, IN_PROGRESS_ISSUES, IN_COMPLETE_ISSUES));

        assertSearchContainsOnly("\"Testing status\" IN (\"Complete\", \"In progress\")", unionStringArrays(IN_PROGRESS_ISSUES, COMPLETE_ISSUES));
        assertSearchContainsOnly("\"Testing status\" NOT IN (\"Complete\", \"In progress\")", unionStringArrays(NOT_STARTED_ISSUES, IN_COMPLETE_ISSUES));
    }

    /**
     * The reason behind this sorting is that we accidentally support ORDER-BY in Lucene.
     * It's simply sorting TestingStatus by the i18n key of each status of an issue.
     * However, there are some typos since the dawn timw of i18n keys, causing this strange ordering behaviour.
     *
     * bonfire.teStingstatus.notstarted = Not started
     * bonfire.teStingstatus.inprogress = In progress
     * bonfire.teXtingstatus.incomplete = Incomplete -- typo
     * bonfire.teXtingstatus.complete = Complete -- typo
     *
     * Therefore, the current order would be
     * IN PROGRESS -> NOT STARTED -> COMPLETE -> IN COMPLETE -> NULL
     */
    @Override
    public void testOrderByClauses() {
        assertSearchContainsInOrder("ORDER BY \"Testing status\"", unionStringArrays(IN_PROGRESS_ISSUES,
                NOT_STARTED_WITH_RELATED_TEST_SESSION_ISSUES,
                COMPLETE_ISSUES,
                IN_COMPLETE_ISSUES,
                EMPTY_TESTING_STATUS_ISSUES));
        assertSearchContainsInOrder("ORDER BY \"Testing status\" ASC", unionStringArrays(IN_PROGRESS_ISSUES,
                NOT_STARTED_WITH_RELATED_TEST_SESSION_ISSUES,
                COMPLETE_ISSUES,
                IN_COMPLETE_ISSUES,
                EMPTY_TESTING_STATUS_ISSUES));
        assertSearchContainsInOrder("ORDER BY \"Testing status\" DESC", unionStringArrays(EMPTY_TESTING_STATUS_ISSUES,
                IN_COMPLETE_ISSUES,
                COMPLETE_ISSUES,
                NOT_STARTED_WITH_RELATED_TEST_SESSION_ISSUES,
                IN_PROGRESS_ISSUES));
    }

    @Override
    public void testSelectClauses() {

    }

    @Override
    public void testGroupByClauses() {

    }

    private static String[] unionStringArrays(String[]... stringArrays) {
        return Stream.of(stringArrays).flatMap(Stream::of).toArray(String[]::new);
    }
}
