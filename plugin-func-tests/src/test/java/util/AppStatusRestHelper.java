package util;

import com.atlassian.applinks.core.Application;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A Test helper for getting and setting the Theme Mode for Studio.
 *
 * @since v3.0
 */
public class AppStatusRestHelper {

    private static final String REST_PATH = "/rest/studio/appstatus/latest";
    public static final String DEFAULT_JIRA_URL = "http://thalassa.sydney.atlassian.com:2990/jira";


    public static HttpResponse setAppStatus(String url, Application application, boolean enabled) {
        return setAppStatus(url, application, enabled, User.SYSADMIN);
    }

    public static HttpResponse setAppStatus(String url, Application application, boolean enabled, User user) {
        return RestUtils.doPost(getAppStatusRestUrl(url, application, enabled, user));
    }


    public static void disableAll(String url) {
        disableAll(url, User.SYSADMIN);
    }

    public static void disableAll(String url, User user) {
        setAppStatus(url, Application.CONFLUENCE, false, user);
        setAppStatus(url, Application.FISHEYE, false, user);
        setAppStatus(url, Application.BAMBOO, false, user);
    }


    private static String getAppStatusRestUrl(String baseUrl, Application application, Boolean enabled, User user) {
        return addCredentialsToUrl(buildAppStatusRestUrl(baseUrl, application, enabled), user);
    }

    private static String buildAppStatusRestUrl(String baseUrl, Application application, Boolean enabled) {
        String applicationsPath = "/applications";
        if (application != null && enabled != null) {
            applicationsPath += "/" + application.name() + "/" + enabled.toString();
        }

        return baseUrl + REST_PATH + applicationsPath;
    }

    private static String addCredentialsToUrl(String url, User user) {
        return url + "?os_username=" + user.getUserName() + "&os_password=" + user.getPassword();
    }

    public static void enableOnly(String url, Application... accessibleApplications) {
        disableAll(url);
        for (Application app : accessibleApplications) {
            setAppStatus(url, app, true);
        }
    }

    public static boolean isJiraStandalone() {
        try {
            return isJiraStandalone(User.SYSADMIN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isJiraStandalone(User user) throws Exception {
        String response = getJiraStatus(user).getContent();
        boolean confluenceEnabled = isAppEnabled(Application.CONFLUENCE, response);
        boolean fisheyeEnabled = isAppEnabled(Application.FISHEYE, response);
        boolean bambooEnabled = isAppEnabled(Application.BAMBOO, response);
        return !confluenceEnabled && !fisheyeEnabled && !bambooEnabled;
    }

    private static boolean isAppEnabled(Application application, String response) throws Exception {

        boolean enabled = false;
        JSONObject responseJson = new JSONObject(response);

        JSONArray applicationsJson = responseJson.getJSONArray("applications");

        for (int i = 0; i < applicationsJson.length(); i++) {
            JSONObject applicationJson = applicationsJson.getJSONObject(i);
            if (applicationJson.getString("application").equals(application.name())) {
                enabled = applicationJson.getBoolean("enabled");
                if (enabled) {
                    break;
                }
            }
        }
        return enabled;
    }

    public static HttpResponse getJiraStatus(User user) throws Exception {
        return RestUtils.doGet(getAppStatusRestUrl(DEFAULT_JIRA_URL, null, null, user));
    }


}
