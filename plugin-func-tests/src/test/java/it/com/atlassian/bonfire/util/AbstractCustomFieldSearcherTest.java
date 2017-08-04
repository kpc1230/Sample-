package it.com.atlassian.bonfire.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.ImmutableList;
import com.atlassian.jira.functest.framework.query.CollectionAggregationQueryAssertions;
import com.atlassian.jira.functest.framework.query.CollectionAggregationQueryHelper;
import com.atlassian.jira.functest.framework.query.QueryModeFeatureFlagsHelper;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.jira.testkit.client.restclient.VSearchCollectionRequest;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * An Abstract class for writing and running Func Tests which test searching for fields using JQL.
 *
 * Some methods copied and converted from JIRA's BaseQueryModeTest and QueryModeTestRule.
 */
public abstract class AbstractCustomFieldSearcherTest extends BonfireFuncTestCase {

    protected QueryModeFeatureFlagsHelper queryModeFeatureFlagsHelper = new QueryModeFeatureFlagsHelper(backdoor);

    protected CollectionAggregationQueryAssertions queryAssertions;

    @Before
    public void resetQueryModeFlags() {
        queryModeFeatureFlagsHelper.resetAllFlags();
    }

    /**
     * Tests everything for the custom field using the database search
     */
    @Test
    public void testDatabaseMode() {
        queryModeFeatureFlagsHelper = new QueryModeFeatureFlagsHelper(backdoor);

        queryModeFeatureFlagsHelper.ensureJqlDbMode(false);
        queryModeFeatureFlagsHelper.ensureApiDbMode(false);
        queryModeFeatureFlagsHelper.ensureSharedEntitiesDbMode(false);
        queryAssertions = new CollectionAggregationQueryAssertions(new CollectionAggregationQueryHelper(backdoor));
        testClauses();
    }

    /**
     * Tests everything for the custom field using the index search.
     * Lucene tests should not run in Vertigo.
     */
    @Test
    @IgnoreInVertigo
    public void testLuceneMode() {
        queryModeFeatureFlagsHelper = new QueryModeFeatureFlagsHelper(backdoor);
        queryModeFeatureFlagsHelper.ensureJqlLuceneMode(false);
        queryModeFeatureFlagsHelper.ensureApiLuceneMode(false);
        queryModeFeatureFlagsHelper.ensureSharedEntitiesLuceneMode(false);
        queryAssertions = new CollectionAggregationQueryAssertions(new CollectionAggregationQueryHelper(backdoor));
        testClauses();
    }

    /**
     * Run all the tests for the custom field
     */
    public void testClauses() {
        testSelectClauses();
        testWhereClauses();
        testOrderByClauses();
        testGroupByClauses();
    }

    /**
     * Run tests against the Where clause for the custom field
     */
    protected abstract void testWhereClauses();

    /**
     * Run tests against the Order By clause for the custom field
     */
    protected abstract void testOrderByClauses();

    /**
     * Run tests against the Select clause for the custom field
     */
    protected abstract void testSelectClauses();

    /**
     * Run tests against the GroupBy clause for the custom field
     */
    protected abstract void testGroupByClauses();

    /**
     * Assert that the given JQL returns a list of expected issue keys, in any order
     *
     * @param jql               The JQL to search with using REST
     * @param expectedIssueKeys The Issue Keys (eg ABC-1) which are expected to be found given the JQL search
     */
    protected void assertSearchContainsOnly(String jql, String... expectedIssueKeys) {
        SearchClient search = backdoor.search();
        assertSearchContainsOnly(jql, search, expectedIssueKeys);
    }

    /**
     * Assert that the given JQL returns no issues.
     *
     *  @param jql               The JQL to search with using REST
     */
    protected void assertSearchContainsNone(String jql) {
        SearchClient search = backdoor.search();
        assertSearchContainsNone(jql, search);
    }

    /**
     * Assert the search returns a response code
     *
     * @param jql          The JQL to search with using REST
     * @param responseCode Expected response code
     */
    protected void assertSearchResponse(String jql, int responseCode) {
        Response response = backdoor.search().getSearchResponse(new SearchRequest().jql(jql));
        assertThat(response.statusCode, is(responseCode));
    }

    /**
     * Assert that the given JQL returns a list of expected issue keys, in any order
     *
     * @param username          The name of the searching user
     * @param password          The password of the searching user
     * @param jql               The JQL to search with using REST
     * @param expectedIssueKeys The Issue Keys (eg ABC-1) which are expected to be found given the JQL search
     */
    protected void assertSearchContainsOnlyAsUser(String username, String password, String jql,
                                                  String... expectedIssueKeys) {
        SearchClient search = backdoor.search().loginAs(username, password);
        assertSearchContainsOnly(jql, search, expectedIssueKeys);
    }

    private void assertSearchContainsOnly(String jql, SearchClient search, String[] expectedIssueKeys) {
        SearchResult searchResult = search.postSearch(new SearchRequest().jql(jql));
        Set<String> foundIssueKeys = searchResult.issues.stream().map(i -> i.key).collect(toSet());

        assertThat("Found issues " + foundIssueKeys + " are in expected list", foundIssueKeys,
                containsInAnyOrder(expectedIssueKeys));
    }

    private void assertSearchContainsNone(String jql, SearchClient search) {
        SearchResult searchResult = search.postSearch(new SearchRequest().jql(jql));
        assertThat(String.format("Found issues (%s) when none expected", searchResult.issues), searchResult.issues, empty());
    }

    /**
     * Assert that the given JQL returns a list of expected issue keys in the order of the given issue keys
     *
     * @param jql               The JQL to search with using REST
     * @param expectedIssueKeys The Issue Keys (eg ABC-1) which are expected to be found given the JQL search
     */
    protected void assertSearchContainsInOrder(String jql, String... expectedIssueKeys) {
        SearchResult searchResult = backdoor.search().postSearch(new SearchRequest().jql(jql));
        List<String> foundIssueKeys = searchResult.issues.stream().map(i -> i.key).collect(toList());

        assertThat("Found issues " + foundIssueKeys + " matches expected list", foundIssueKeys,
                contains(expectedIssueKeys));
    }

    /**
     * Assert that the given JQL returns expected results containing Issue Key and requested field value
     *
     * @param jql                       The JQL to search with using REST
     * @param fieldName                 The custom field name to include in results
     * @param expectedIssueKeysAndField Map with Issue Keys (eg ABC-1) as Key and Field Value as Value which are expected to be found given the API search
     */
    protected <T> void assertSelectContainsOnly(final String jql, final String fieldName,
                                                final Map<String, T> expectedIssueKeysAndField) {
        final String customFieldId = backdoor.customFields().getCustomFieldByName(fieldName).map(cf -> cf.id)
                .orElseThrow(() -> new RuntimeException(
                        "No custom field found with name '" + fieldName + "'"));

        final VSearchCollectionRequest searchRequest = new VSearchCollectionRequest()
                .fields(ImmutableList.of(customFieldId, "issuekey")).jql(jql);

        final SearchResult search = backdoor.vsearch().getSearch(searchRequest);

        final Map<String, T> foundResults = search.issues.stream().collect(
                toMap(issue -> issue.key, issue -> issue.fields.<T>get(customFieldId)));

        // compare the entry sets in any order
        assertThat("Found issues " + foundResults + " are in expected results", foundResults.entrySet(),
                containsInAnyOrder(
                        expectedIssueKeysAndField.entrySet().stream().map(Matchers::equalTo).collect(toSet())));
    }

    /**
     * Constructs a map with three key-value pairs. Needed because ImmutableMap is null-hostile.
     */
    public static <T> Map<T, Object> mapOf(T k1, Object v1, T k2, Object v2, T k3, Object v3) {
        Map<T, Object> map = mapOf(k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }

    /**
     * Constructs a map with two key-value pairs. Needed because ImmutableMap is null-hostile.
     */
    public static <T> Map<T, Object> mapOf(T k1, Object v1, T k2, Object v2) {
        Map<T, Object> map = mapOf(k1, v1);
        map.put(k2, v2);
        return map;
    }

    /**
     * Constructs a map with one key-value pair. Needed because ImmutableMap is null-hostile.
     */
    public static <T> Map<T, Object> mapOf(T k1, Object v1) {
        Map<T, Object> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }
}
