package com.thed.zephyr.capture.util;

public class ApplicationConstants {

    public static final String MDC_TENANTKEY = "tenantKey";
    public static final String MDC_CAPID = "capid";
    public static final String PLUGIN_KEY = "addonKey";
    public static final String LICENSE_PING_JOB = "license.ping.job";
    public static final String CAPTURE_CONNECT_BASE_URL = "connect.baseUrl";
    public static final String CAPTUREUI_BASE_URL = "captureui.baseUrl";
    public static final String DYNAMIC_PROPERTY_CONFIG_FILE = "dynamic.prop.file.name";
    public static final String DYNAMIC_PROPERTY_CONFIG_URLS = "dynamic.prop.urls";
    public static final String SYSTEM_KEY = "System" ;
    public static final String LICENSE_PING_DEFAULT_CRON_EXPR = "0 0 */4 * * *";
    public static final String LICENSE_PING_CRON_EXPR = "license.ping.cron.expression" ;

    /** DynamoDB table names **/
    public static final String TENANT_TABLE_NAME = "tenant";
    public static final String SESSION_TABLE_NAME = "session";
    public static final String NOTE_TABLE_NAME = "note";
    public static final String TEMPLATE_TABLE_NAME = "template";
    public static final String VARIABLE_TABLE_NAME = "variable";
    public static final String SESSION_ACTIVITY_TABLE_NAME = "session_activity";

    /** DynamoDB table throughput **/
    public static final long TENANT_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long TENANT_TABLE_WRITE_CAPACITY_UNITS = 2l;
    public static final long SESSION_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long SESSION_TABLE_WRITE_CAPACITY_UNITS = 2l;
    public static final long TEMPLATE_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long TEMPLATE_TABLE_WRITE_CAPACITY_UNITS = 2l;
    public static final long SESSION_ACTIVITY_TABLE_WRITE_CAPACITY_UNITS = 2l;
    public static final long SESSION_ACTIVITY_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long VARIABLE_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long VARIABLE_TABLE_WRITE_CAPACITY_UNITS = 2l;
    public static final long NOTE_TABLE_READ_CAPACITY_UNITS = 2l;
    public static final long NOTE_TABLE_WRITE_CAPACITY_UNITS = 2l;

    /** DynamoDB global secondary indexes **/
    public static final String GSI_CLIENT_KEY = "idx_client_key";
    public static final String GSI_CT_ID_PROJECT_ID = "idx_ctId_projectid";
    public static final String GSI_BASE_URL = "idx_base_url";
    public static final String GSI_CT_ID_CREATED_BY = "idx_ct_id_created_by";
    public static final String GSI_CT_ID_SHARED = "idx_ct_id_shared";
    public static final String GSI_PROJECTID = "idx_project_id";
    public static final String GSI_SESSIONID_TIMESTAMP = "idx_session_id_timestamp";
    public static final String GSI_CT_ID_SESSION_ID = "idx_ct_id_session_id";
    public static final String GSI_CT_ID_OWNER_NAME = "idx_ct_id_owner_name";


    public static final String BROWSER_FIREFOX_EXTENSION_DOWNLOAD = "browser.firefox.extension.download.url";
    public static final String BROWSER_SAFARI_EXTENSION_DOWNLOAD = "browser.safari.extension.download.url";
    public static final String BROWSER_IE_EXTENSION_DOWNLOAD = "browser.ie.extension.download.url";
    public static final String BROWSER_CHROME_EXTENSION_DOWNLOAD = "browser.chrome.extension.download.url";
    public static final String DOWNLOAD_URL = "downloadUrl" ;
    public static final String BROWSER = "browser" ;
    public static final String BROWSER_CHROME = "chrome";
    public static final String BROWSER_FIREFOX = "firefox";
    public static final String BROWSER_MSIE = "msie";
    public static final String BROWSER_MSIE_ALT = "trident/"; // IE11 browser has name Mozilla and can be identified by the keyword

    public static final String BROWSER_SAFARI = "safari";
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";

    //   public static final String GSI_STATUS = "idx_status";

    /*** LockService ***/
    public static final String HZ_LOCK_IMAP_NAME = "capture_cloud_lock_imap";
    public static final Integer DEFAULT_LOCK_TIMEOUT_SEC = 5;
    public static final Integer MAX_LOCK_TIMEOUT_SEC = 300;

    public static final String VERSIONS = "versions";
    public static final String CAPTURE_BASEURL = "baseUrl";
    public static final String AES_ENCRYPTION_SECRET_KEY = "aes.encryption.secret.key";
    public static final String HEADER_PARAM_PACCESS_KEY = "accessKey";
    public static final String USER_AGENT = "User-Agent";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BE_ACCESS_KEY_EXPIRATION_TIME = "be.accessKey.expiration.time.milliSeconds";
    public static final String CREATE_ATTACHMENT_PERMISSION = "CREATE_ATTACHMENT";
    public static final String CREATE_ISSUE_PERMISSION = "CREATE_ISSUE";
    public static final String EDIT_ISSUE_PERMISSION = "EDIT_ISSUE";
    public static final String BROWSE_PROJECT_PERMISSION = "BROWSE_PROJECTS";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String ASSIGNABLE_USER = "ASSIGNABLE_USER";
    public static final String PROJECT_ADMIN = "PROJECT_ADMIN";
    public static final String SESSION = "session";
    public static final String RAISED_ISSUE = "raisedIssues";
    public static final String RELATED_ISSUE = "relatedIssues";
    public static final String SESSION_ACTIVITIES = "sessionActivities";

    /*** for MetadataField ***/
    public static String PROJECTS = "projects";
    public static String ALLOWED_VALUES = "allowedValues";
    public static String FIELDS = "fields";
    public static String ISSUE_TYPES = "issuetypes";
    public static String ISSUE_TYPE = "issueType";
    public static String KEY = "key";
    public static String OPTIONS = "options";
    public static String FIELD_LIST_BEANS = "fieldListBeans";
    public static String FIELD_DETAILS = "fieldDetails";
    public static String USER_BEANS = "userBeans";
    
    public static int MAX_NOTE_LENGTH = 2000;
    public static final int RELATED_ISSUES_LIMIT = 100;
    public static final Integer DEFAULT_RESULT_SIZE = 20 ;
    
    public static final String RELATED_ISSUES_LIMIT_DYNAMIC_KEY = "session.issue.max.limit";
    public static final String PARTICIPANT_LIMIT_DYNAMIC_KEY = "session.participant.limit";
    
    public static final String SORTFIELD_CREATED = "created";
    public static final String SORTFIELD_SESSION_NAME = "sessionname";
    public static final String SORTFIELD_PROJECT = "project";
    public static final String SORTFIELD_ASSIGNEE = "assignee";
    public static final String SORTFIELD_STATUS = "status";
    public static final String SORTFIELD_SHARED = "shared";
    
    public static final String SORT_ASCENDING = "ASC";
    public static final String TENANT_ID_FIELD = "ctId";
    public static final String PROJECT_ID = "projectId";
    public static final String ASSIGNEE_FIELD = "assignee";
    public static final String STATUS_FIELD = "status";
    public static final String SESSION_NAME_FIELD = "name";
    public static final String SHARED_FIELD = "shared";

    /** email fields **/
    public static final String FROM_EMAIL = "aws.ses.from";
    public static final String AWS_SES_REGION = "aws.ses.region";
    public static final String AWS_SMTP_USERNAME = "aws.smtp.username";
    public static final String AWS_SMTP_PASSWORD = "aws.smtp.password";
    public static final String AWS_SMTP_PORT = "aws.smtp.port";
    public static final String DEFAULT_EMAIL_FROM = "masudur.rahman@getzephyr.com";
    public static final String DEFAULT_SMTP_USERNAME = "AKIAJJVT37JAF3R6VJ4Q";
    public static final String DEFAULT_SMTP_PASSWORD = "ApnTtFsyojiOKBMfDGj8+cPlw7eISDrYIEwQQ6ulXmI1";
    public static final int DEFAULT_SMTP_PORT = 465;
    public static final String DEFAULT_SES_REGION = "us-west-2";
    
    public static final String SESSION_ID_FIELD = "sessionId";

    public static final String VERSION_PING_DEFAULT_CRON_EXPR = "0 0 0 * * *";
    public static final String VERSION_PING_CRON_EXPR = "version.ping.cron.expression" ;
    public static final String PING_HOME_URL = "app.dial.home.url";
    public static final String PING_HOME_VERSION_CHECK_URL = "https://version.yourzephyr.com/capture_version_check.php";

    public static final int DEFAULT_CACHE_EXPIRATION = 60; //in Seconds
    public static final int MEDIUM_CACHE_EXPIRATION = 180; //in Seconds
    public static final int FIVE_MIN_CACHE_EXPIRATION = 300; //in Seconds
    public static final int FOUR_HOUR_CACHE_EXPIRATION = 4*60*60; //in Seconds
    public static final int MAX_CACHE_EXPIRATION = 259200; //in seconds (3 days)

    /** Issue cache const **/
    public static final String ISSUE_CACHE_KEY_PREFIX = "issue-";
    public static final String ISSUE_CACHE_EXPIRATION_DYNAMIC_PROP = "issue.cache.expiration";
    /** Permission cache const **/
    public static final String PERMISSION_CACHE_KEY_PREFIX = "permission-";
    public static final String PERMISSION_CACHE_EXPIRATION_DYNAMIC_PROP = "permission.cache.expiration";
    /** Project cache const **/
    public static final String PROJECT_CACHE_KEY_PREFIX = "project-";
    public static final String PROJECT_CACHE_EXPIRATION_DYNAMIC_PROP = "project.cache.expiration";
    /** User cache const **/
    public static final String USER_CACHE_KEY_PREFIX = "user-";
    public static final String USER_CACHE_EXPIRATION_DYNAMIC_PROP = "user.cache.expiration";

    public static final String SESSION_LOCK_KEY = "session_";

    public static final String SESSION_URL_TEMPLATE = "/plugins/servlet/ac/capture-cloud/view-session-url?session.id=%s&origin=nav&invite=true";
}
