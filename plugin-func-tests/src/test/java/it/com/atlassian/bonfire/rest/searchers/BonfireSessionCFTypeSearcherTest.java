package it.com.atlassian.bonfire.rest.searchers;

import it.com.atlassian.bonfire.util.AbstractCustomFieldSearcherTest;

public class BonfireSessionCFTypeSearcherTest extends AbstractCustomFieldSearcherTest {

    @Override
    public void setUpTest() {
        restoreData("capture-raisedduring-searcher.xml");
        runUpgradeTasks();
    }

    @Override
    public void testWhereClauses() {
        assertSearchContainsOnly("\"Raised during\" IS EMPTY", "HSP-6");
        assertSearchContainsOnly("\"Raised during\" IS NOT EMPTY", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" =  EMPTY", "HSP-6");
        assertSearchContainsOnly("\"Raised during\" != EMPTY", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" = \"10006\"", "HSP-1", "HSP-2");
        assertSearchContainsOnly("\"Raised during\" != \"10006\"", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" IN (\"10006\")", "HSP-1", "HSP-2");
        assertSearchContainsOnly("\"Raised during\" NOT IN (\"10006\")", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" IN (EMPTY)", "HSP-6");
        assertSearchContainsOnly("\"Raised during\" NOT IN (EMPTY)", "HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" IN (\"10006\", EMPTY)", "HSP-1", "HSP-2", "HSP-6");
        assertSearchContainsOnly("\"Raised during\" NOT IN (\"10006\", EMPTY)", "HSP-3", "HSP-4", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" IN (\"10006\", \"10011\")", "HSP-1", "HSP-2", "HSP-3", "HSP-4");
        assertSearchContainsOnly("\"Raised during\" NOT IN (\"10006\", \"10011\")", "HSP-5", "HSP-7");

        assertSearchContainsOnly("\"Raised during\" = myActiveSession()", "HSP-7");
        assertSearchContainsOnly("\"Raised during\" = \"10006\" and description is EMPTY and project = HSP", "HSP-1", "HSP-2");

        assertSearchContainsOnly("\"Raised during\" IN (EMPTY, \"10006\")", "HSP-1", "HSP-2", "HSP-6");
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
