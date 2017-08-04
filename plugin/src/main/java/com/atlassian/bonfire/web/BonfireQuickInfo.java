package com.atlassian.bonfire.web;

import com.atlassian.excalibur.web.ExcaliburWebActionSupport;

/**
 * This exists to work around Safari bug where is needs to you visit a site in order to set cookies for that site
 * So we &lt;iframe src="" this page to perform the workaround.  Its lightweight and says fuck all!
 */
@SuppressWarnings("serial")
public class BonfireQuickInfo extends ExcaliburWebActionSupport {
    public String doInfo() {
        return SUCCESS;
    }
}
