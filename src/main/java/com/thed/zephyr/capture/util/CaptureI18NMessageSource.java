package com.thed.zephyr.capture.util;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * This class is used inside the capture application to avoid 
 * getting locale in all the caller methods.
 * @author venkatareddy on 09/07/2017
 *
 */
@Component
public class CaptureI18NMessageSource extends ResourceBundleMessageSource {

    public String getMessage(String code)throws NoSuchMessageException{
        return this.getMessage(code, null);
    }

    public String getMessage(String code, Object[] args)throws NoSuchMessageException{
        return super.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}
