package com.thed.zephyr.capture.properties;

import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.excalibur.web.util.VersionKit.SoftwareVersion;

public class BonfireConstants {
    public static final String TRACKING_CODE = "UA-20272869-8";

    public static final int SESSION_NAME_LENGTH_LIMIT = 500;

    public static final int ADDITIONAL_INFO_LENGTH_LIMIT = 20000;

    public static final int RELATED_ISSUES_LIMIT = 100;

    public static final int MAX_NOTE_LENGTH = 2000;

    public static final String SESSION_PAGE = "/secure/ViewSession.jspa?testSessionId=";

    public static final String SESSION_NAV_PAGE = "/secure/SessionNavigator.jspa";

    public static final String GETTING_STARTED_PAGE = "/secure/BonfireGettingStarted.jspa";

    public static final String GET_BONFIRE_PAGE = "/secure/GetBonfire.jspa";

    public static final String ADVANCED_CFT_PAGE = "/secure/AdvancedCFT.jspa?";

    public static final String SORTFIELD_CREATED = "created";
    public static final String SORTFIELD_SESSION_NAME = "sessionname";
    public static final String SORTFIELD_PROJECT = "project";
    public static final String SORTFIELD_ASSIGNEE = "assignee";
    public static final String SORTFIELD_STATUS = "status";
    public static final String SORTFIELD_SHARED = "shared";

    // Not a real status
    public static final String INCOMPLETE_STATUS = "INCOMPLETE";

    // This is the lowest version of JIRA that we currently support
    public static final VersionKit.SoftwareVersion LOWEST_SUPPORTED_JIRA = new SoftwareVersion(5, 0);
}
