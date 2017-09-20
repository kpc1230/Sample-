package com.thed.zephyr.capture.util;


public class CaptureConstants {
    public static final String TRACKING_CODE = "UA-20272869-8";

    public static final int SESSION_NAME_LENGTH_LIMIT = 500;

    public static final int ADDITIONAL_INFO_LENGTH_LIMIT = 20000;

    public static final int RELATED_ISSUES_LIMIT = 100;

    public static final int MAX_NOTE_LENGTH = 2000;

    public static final String SORTFIELD_CREATED = "created";
    public static final String SORTFIELD_SESSION_NAME = "sessionname";
    public static final String SORTFIELD_PROJECT = "project";
    public static final String SORTFIELD_ASSIGNEE = "assignee";
    public static final String SORTFIELD_STATUS = "status";
    public static final String SORTFIELD_SHARED = "shared";

    // Not a real status
    public static final String INCOMPLETE_STATUS = "INCOMPLETE";

    public static final String OS_ICON = "osIcon";
    public static final String BROWSER_CHROME = "chrome";
    public static final String BROWSER_FIREFOX = "firefox";
    public static final String BROWSER_MSIE = "msie";
    public static final String BROWSER_MSIE_ALT = "trident/"; // IE11 browser has name Mozilla and can be identified by the keyword

    public static final String BROWSER_SAFARI = "safari";
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";

    public static final String OS_MAC = " mac ";
    public static final String BLUE = "blue";
    public static final String YELLOW = "yellow";
}
