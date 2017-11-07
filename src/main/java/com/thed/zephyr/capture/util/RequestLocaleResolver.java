package com.thed.zephyr.capture.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Locale Resolver depending on the request.
 * @author venkatareddy
 *
 */
public class RequestLocaleResolver extends AcceptHeaderLocaleResolver {
	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		String localeString = request.getParameter("loc"); 
        Locale preferredLocale = null;
        if (localeString != null) {
        	int indexOfUnderscore = localeString.indexOf('-'); 
            if (indexOfUnderscore != -1) { 
                String language = localeString.substring(0, indexOfUnderscore); 
                String country = localeString.substring(indexOfUnderscore + 1); 
                preferredLocale = new Locale(language, country); 
            } else { 
                preferredLocale = new Locale(localeString); 
            }
        }else{
        	preferredLocale = super.resolveLocale(request);
        }
        LocaleContextHolder.setLocale(preferredLocale, true);
        
	    return preferredLocale;
	}

}
