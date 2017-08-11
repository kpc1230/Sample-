package com.thed.zephyr.capture.serverInfo;

import org.apache.commons.lang3.StringUtils;

public class AddonServerInfo {

    public AddonServerInfo() {
    }

    private String version;

    private String name;

    public String getVersion() {
        return StringUtils.replace(version, "-g", "-");
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "version:" + version + " name:" + name;
    }
}
