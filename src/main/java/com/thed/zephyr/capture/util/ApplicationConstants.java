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
    public static final String GSI_CTID_PROJECTID = "idx_ctId_projectid";
    public static final String GSI_BASE_URL = "idx_base_url";
    public static final String GSI_CREATED_BY = "idx_created_by";
    public static final String GSI_SHARED = "idx_shared";
    public static final String GSI_PROJECTID = "idx_project_id";
    public static final String GSI_SESSIONID_TIMESTAMP = "idx_session_id_timestamp";
    public static final String GSI_CT_ID_SESSION_ID = "idx_ct_id_session_id";


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
    public static final Object CREATE_ATTACHMENT_PERMISSION = "CREATE_ATTACHMENT";

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

}
