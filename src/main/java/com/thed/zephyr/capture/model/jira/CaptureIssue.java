package com.thed.zephyr.capture.model.jira;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;

import java.io.Serializable;
import java.net.URI;

/**
 * Created by niravshah on 8/30/17.
 */
public class CaptureIssue extends BasicIssue implements Serializable {
    private String iconPath;

    public CaptureIssue(URI self, String key, Long id, String iconPath){
        super(self,key,id);
        this.iconPath=iconPath;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

}
