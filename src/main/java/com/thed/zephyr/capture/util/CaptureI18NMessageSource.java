package com.thed.zephyr.capture.util;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * This class is used inside the capture application to avoid
 * getting locale in all the caller methods.
 *
 * @author venkatareddy on 09/07/2017
 */
@Component
public class CaptureI18NMessageSource extends ResourceBundleMessageSource {

    private Map<String, Map<String, String>> loadedBundels = new HashMap();
    private Set baseNames;

    public String getMessage(String code) throws NoSuchMessageException {
        return this.getMessage(code, null);
    }

    public String getMessage(String code, Object[] args) throws NoSuchMessageException {
        return super.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public Set<String> getKeys(String basename, Locale locale) {
        ResourceBundle bundle = getResourceBundle(basename, locale);
        return bundle.keySet();
    }

    public Map getKeyValues(String basename, Locale locale) {
        String cacheKey = basename + locale.getDisplayLanguage() + locale.getCountry();
        if (loadedBundels.containsKey(cacheKey)) {
            return loadedBundels.get(cacheKey);
        }
        ResourceBundle bundle = getResourceBundle(basename, locale);
        TreeMap treeMap = new TreeMap();
        for (String key : bundle.keySet()) {
            treeMap.put(key, getMessage(key, null, locale));
        }
        loadedBundels.put(cacheKey, treeMap);
        return treeMap;
    }

}
