package com.thed.zephyr.capture.util;

import com.netflix.config.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Masud on 11/7/16.
 */
@Component
public class DynamicProperty {

    @Autowired
    private Logger log;

    @Autowired
    private DynamicPropertyFactory instance;

    @Autowired
    private Environment env;

    public DynamicProperty() {
    }

    public DynamicLongProperty getLongProp(String key, long defaultValue) {
        return instance.getLongProperty(key, defaultValue);
    }

    public DynamicBooleanProperty getBoolProp(String key, Boolean defaultValue) {
        return instance.getBooleanProperty(key, defaultValue);
    }

    public DynamicStringProperty getStringProp(String key, String defaultValue) {
        return instance.getStringProperty(key, defaultValue);
    }

    public DynamicStringListProperty getStringListProp(String key, List<String> defaultValue){
        DynamicStringListProperty dynamicStringListProperty = new DynamicStringListProperty(key, defaultValue);
        return dynamicStringListProperty;
    }

    public DynamicIntProperty getIntProp(String key, int defaultValue){
        return instance.getIntProperty(key, defaultValue);
    }

    public DynamicStringProperty registerListener(String property, Runnable callback){
        DynamicStringProperty stringProperty = instance.getStringProperty(property, "");
        log.debug("Add listener to prop:{} current value:{}", property, stringProperty.get());
        stringProperty.addCallback(callback);
        return stringProperty;
    }

}
