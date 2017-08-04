package util;

import com.atlassian.applinks.core.Application;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class User {
    private static final Logger log = LoggerFactory.getLogger(User.class);

    public static final List<Application> DEVELOPER_ACCESS = ImmutableList.of(Application.JIRA, Application.CONFLUENCE,
            Application.FISHEYE, Application.BAMBOO, Application.CRUCIBLE, Application.SVN);
    public static final List<Application> COLLABORATOR_ACCESS = ImmutableList.of(Application.JIRA, Application.CONFLUENCE);
    public static final List<Application> NO_ACCESS = ImmutableList.of();


    public static final User SYSADMIN = builder().username("sysadmin").
            onDemandPassword("sysadmin").
            fullName("System Administrator").
            email("noreply@atlassian.com").
            granted(COLLABORATOR_ACCESS).
            toUser();
    //    /**
//     * Admin user used to set up test fixture and test stuff that's not GAPPS-related. This user is shortcut to login
//     * via Seraph as its openId is set to false. Use {@link #ADMIN_PROPER} if you need to test GAPPS-specific stuff,
//     * such as login via Google, or you need to test against the openId attribute.
//     */
//    public static final User ADMIN = builder().username("admin").
//            onDemandPassword("admin").
//            fullName(gappsEnabled ? "Michael Knighten" : "Administrator").
//            granted(DEVELOPER_ACCESS).
//            toUser();
//    /**
//     * Admin user used to test GAPPS-specific stuff, such as login via Google, or you need to test against the openId
//     * attribute. Use {@link #ADMIN} as much as possible to reduce the likeliness of Google CAPTCHA challenge.
//     */
//    public static final User ADMIN_PROPER = builder().username("admin").
//            onDemandPassword("admin").
//            opendIdPassword("atl12345").
//            fullName(gappsEnabled ? "Michael Knighten" : "Administrator").
//            granted(DEVELOPER_ACCESS).
//            toUser();
//    @Deprecated // Use DEVELOPER or COLLABORATOR
//    public static final User FRED = builder().username("fred").
//            onDemandPassword("fred").
//            opendIdPassword("password").
//            fullName("Fred Normal").
//            granted(DEVELOPER_ACCESS).
//            toUser();
//    public static final User COLLABORATOR = builder().username("collaborator").
//            onDemandPassword("collaborator").
//            opendIdPassword("password").
//            fullName("A Collaborator").
//            granted(COLLABORATOR_ACCESS).
//            toUser();
//    public static final User DEVELOPER = builder().username("developer").
//            onDemandPassword("developer").
//            opendIdPassword("password").
//            fullName("A Developer").
//            granted(DEVELOPER_ACCESS).
//            toUser();
//    @Deprecated // Use DEVELOPER or COLLABORATOR
//    public static final User TEST = builder().username("test").
//            onDemandPassword("test").
//            fullName("Test User").
//            email("test@example.com").
//            granted(DEVELOPER_ACCESS).
//            toUser();
//
//    public static final User[] TEST_USERS = {ADMIN_PROPER, FRED, COLLABORATOR, DEVELOPER, TEST};
//
//    // This user has been created to test that the "Google User - needs activation" user type is displayed in the
//    // studio user browser on gapps instances for google users who have not yet logged into studio.
//    public static final User STUDIO_GOOGLE_USER = new User("studiogoogleuser", "password", "Google User", AccessLevel.DEVELOPER_ACCESS);
//
//    public static final String FRED_EMAIL = FRED.getEmail();
//
    private final String userName;
    private final String password;
    private final String fullName;
    private final Collection<Application> applications;
    private final String email;
    private final String openIdPassword;

    public static Builder builder() {
        return new Builder();
    }

    public User(String userLoginName, String userPassword, String userFullname, Collection<Application> applications, String userEmail) {
        this.userName = userLoginName;
        this.password = userPassword;
        this.fullName = userFullname;
        this.email = userEmail;
        this.openIdPassword = null;
        this.applications = applications;

    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public static class Builder {
        private String username;
        private String onDemandPassword;
        private String fullName;
        private Collection<Application> applications;
        private String email;
        private Boolean openIdUser = Boolean.FALSE;
        private String openIdPassword;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder onDemandPassword(String onDemandPassword) {
            this.onDemandPassword = onDemandPassword;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder granted(Collection<Application> applications) {
            this.applications = applications;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder openIdUserByEmail() {
            this.openIdUser = null;
            return this;
        }

        public Builder openIdUser(boolean openIdUser) {
            this.openIdUser = openIdUser;
            return this;
        }

        public Builder opendIdPassword(String openIdPassword) {
            if (openIdPassword != null) {
                this.openIdUser = true;
            }
            this.openIdPassword = openIdPassword;
            return this;
        }

        public User toUser() {

            return new User(checkUsername(), checkOnDemandPassword(), checkFullName(), granted(), email());
        }

        private String checkUsername() {
            return checkNotNull(username);
        }

        private String checkOnDemandPassword() {
            return checkNotNull(onDemandPassword);
        }

        private String checkFullName() {
            return checkNotNull(fullName);
        }

        private Collection<Application> granted() {
            return applications == null ? NO_ACCESS : applications;
        }

        private String email() {
            if (email == null) {
                email = checkUsername() + "@example.com";
            }
            return email.toLowerCase();
        }

    }
}
