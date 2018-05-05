package com.thed.zephyr.capture.repositories.elasticsearch;

import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.springframework.stereotype.Component;

@Component("indexResolverBean")
public class IndexResolver {

    static private Boolean appStarted = false;

    private String index;

    public IndexResolver() {
    }

    public String getIndex() {
        if(!appStarted){
            return ApplicationConstants.ES_INDEX_NAME;
        }

        return CaptureUtil.getCurrentCtId();
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setAppStarted(Boolean appStarted) {
        this.appStarted = appStarted;
    }
}
