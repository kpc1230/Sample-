package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.annotation.LicenseCheck;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.springframework.core.env.Environment;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Map;

@Controller
public class GadgetController {
    @Autowired
    private Logger log;

    @Autowired
    private DynamicProperty dynamicProperty;

     @Autowired
    private Environment env;

    @Autowired
    private CaptureI18NMessageSource i18n;

    @LicenseCheck
    @RequestMapping(value = "/public/html/gadget")
    public String renderGagdets(@RequestParam String dashboardView, @RequestParam String dashboardId, @RequestParam String dashboardItem, @RequestParam String gadgetType, @RequestParam String xdm_e, @RequestParam String cp, Model model) {
        log.debug("Render Gagdets method is called with Params dashboardView : " + dashboardView + " dashboardId: " + dashboardId + " dashboardItem: " + dashboardItem + " gadgetType: " + gadgetType + " xdm_e: " + xdm_e + " cp:" + cp);
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        model.addAttribute("dashboardView", dashboardView);
        model.addAttribute("dashboardId", dashboardId);
        model.addAttribute("dashboardItem", dashboardItem);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("gadgetType", gadgetType);
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("xdm_e", xdm_e);
        model.addAttribute("cp", cp);

        Locale locale = LocaleContextHolder.getLocale();
        String basename = "i18n/capture-i18n";
        Map<String, String> messages = i18n.getKeyValues(basename, locale);
        model.addAttribute("messages", messages);

        log.debug("Render Gadge method ended");
        return "gadgets";
    }
}
