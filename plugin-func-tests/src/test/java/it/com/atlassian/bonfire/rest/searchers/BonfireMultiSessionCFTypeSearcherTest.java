package it.com.atlassian.bonfire.rest.searchers;

import it.com.atlassian.bonfire.util.AbstractCustomFieldSearcherTest;

public class BonfireMultiSessionCFTypeSearcherTest extends AbstractCustomFieldSearcherTest {

    @Override
    public void setUpTest() {
        restoreData("capture-testsessions-searcher.xml");
        runUpgradeTasks();
    }

    @Override
    public void testWhereClauses() {
        assertSearchContainsOnly("\"Test sessions\" IS EMPTY", "HSP-10", "HSP-9", "HSP-8");
        assertSearchContainsOnly("\"Test sessions\" IS NOT EMPTY", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" =  EMPTY", "HSP-10", "HSP-9", "HSP-8");
        assertSearchContainsOnly("\"Test sessions\" != EMPTY", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" = \"10006\"", "HSP-1", "HSP-2", "HSP-3");
        assertSearchContainsOnly("\"Test sessions\" != \"10006\"", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" IN (\"10006\")", "HSP-1", "HSP-2", "HSP-3");
        assertSearchContainsOnly("\"Test sessions\" NOT IN (\"10006\")", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" IN (EMPTY)", "HSP-10", "HSP-9", "HSP-8");
        assertSearchContainsOnly("\"Test sessions\" NOT IN (EMPTY)", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" IN (\"10006\", EMPTY)", "HSP-1", "HSP-2", "HSP-3", "HSP-10", "HSP-9", "HSP-8");
        assertSearchContainsOnly("\"Test sessions\" NOT IN (\"10006\", EMPTY)", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" IN (\"10006\", \"10007\")", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6");
        assertSearchContainsOnly("\"Test sessions\" NOT IN (\"10006\", \"10007\")", "HSP-7");

        assertSearchContainsOnly("\"Test sessions\" IN (EMPTY, \"10006\")", "HSP-1", "HSP-2", "HSP-3", "HSP-10", "HSP-9", "HSP-8");
        assertSearchContainsOnly("\"Test sessions\" NOT IN (EMPTY, \"10006\")", "HSP-4", "HSP-5", "HSP-6", "HSP-7");

        assertSearchContainsOnly("(\"Test sessions\" = \"10006\" or \"Test sessions\" = \"10007\") and \"Test sessions\" != \"10008\" ", "HSP-1", "HSP-2", "HSP-5", "HSP-6");
    }

    @Override
    public void testOrderByClauses() {

    }

    @Override
    public void testSelectClauses() {

    }

    @Override
    public void testGroupByClauses() {

    }
}
