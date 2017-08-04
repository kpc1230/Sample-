package it.com.atlassian.bonfire.rest.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Test;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;
import com.atlassian.bonfire.features.CaptureFeatureFlags;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import it.com.atlassian.bonfire.util.IgnoreInVertigo;

import static org.junit.Assert.assertThat;

//TODO remove if statements once TestingStatus is migrated and TESTING_STATUS_DB_PRIMARY is removed
public class TestingStatusJQLSearchTest extends BonfireFuncTestCase {

    private SearchClient searchClient;

    @Override
    public void setUpTest() {
        restoreData("capture-testingstatus-searcher.xml");
        searchClient = new SearchClient(getEnvironmentData()).loginAs("admin");

    }


    public void testSearchIssuesByTestingStatusWithJQL() throws IOException {
        //temporary hack as couldn't make IgnoreInVertigoRule work in Capture's Rest Tests
        if(!environmentData.isVertigoMode()) {
            backdoor.featureFlags().disableBool(CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
            backdoor.featureFlags().disableBool(CaptureFeatureFlags.TESTING_STATUS_UPDATE_IN_DB.asFlag().getFeatureKey());
            verifyJQLSearchResults();
        }
    }

    public void testSearchIssuesByTestingStatusWithJQLInDBMode() throws IOException {
        //temporary hack as couldn't make IgnoreInVertigoRule work in Capture's Rest Tests
        if(!environmentData.isVertigoMode()) {
            backdoor.featureFlags().enableBool(CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
            backdoor.featureFlags().enableBool(CaptureFeatureFlags.TESTING_STATUS_UPDATE_IN_DB.asFlag().getFeatureKey());
            verifyJQLSearchResults();
        }
    }

    private void verifyJQLSearchResults() {
        assertJqlResult("\"Testing status\" = \"Not started\"" , Arrays.asList("HSP-8", "HSP-9", "HSP-10", "HSP-11", "HSP-12", "HSP-13", "HSP-14", "HSP-15"));
        assertJqlResult("\"Testing status\" = \"In progress\"" , Arrays.asList("HSP-4", "HSP-5", "HSP-6", "HSP-7"));
        assertJqlResult("\"Testing status\" = Incomplete" , Arrays.asList("HSP-3"));
        assertJqlResult("\"Testing status\" = Complete " , Arrays.asList("HSP-1", "HSP-2"));
    }

    private void assertJqlResult(String jql, List<String> issues) {
        SearchResult searchResults = searchClient.postSearch(new SearchRequest().jql(jql));

        List<String> foundIssues = searchResults.issues
                .stream()
                .map(i -> i.key)
                .collect(Collectors.toList());

        assertThat(issues, Matchers.containsInAnyOrder(foundIssues.toArray()));
    }
}
