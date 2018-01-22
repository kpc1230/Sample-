package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.annotation.LicenseCheck;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.UserAgentSniffer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Masud on 8/15/17.
 */
@Controller
public class GetExtensionController {

    @Autowired
    private Logger log;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private AddonInfoService addonInfoService;

    @LicenseCheck
    @RequestMapping(value = "/get-browser-extension", method = RequestMethod.GET)
    public String getExtensionPage(@AuthenticationPrincipal AtlassianHostUser hostUser, HttpServletRequest request, Model model){
        String userAgent = request.getHeader("user-agent");
        UserAgentSniffer.SniffedBrowser userAgentSniffer = UserAgentSniffer.sniffBrowser(userAgent);
        String browser = userAgentSniffer.browser.toLowerCase();
        model.addAttribute(ApplicationConstants.BROWSER,userAgentSniffer.browser);
        String downloadUrl = "";

        switch(browser) {
            case ApplicationConstants.BROWSER_FIREFOX :
                  downloadUrl = dynamicProperty.getStringProp(
                        ApplicationConstants.BROWSER_FIREFOX_EXTENSION_DOWNLOAD, "").getValue();
                break;
            case ApplicationConstants.BROWSER_SAFARI:
                downloadUrl = dynamicProperty.getStringProp(
                        ApplicationConstants.BROWSER_SAFARI_EXTENSION_DOWNLOAD, "").getValue();
                break;
            case ApplicationConstants.BROWSER_MSIE:
                if (userAgent.contains("Win64") || userAgent.contains("IA64") || userAgent.contains("x64") || userAgent.contains("WOW64")) {
                    downloadUrl = dynamicProperty.getStringProp(
                            ApplicationConstants.BROWSER_IE_64_EXTENSION_DOWNLOAD, "").getValue();
                } else {
                    downloadUrl = dynamicProperty.getStringProp(
                            ApplicationConstants.BROWSER_IE_32_EXTENSION_DOWNLOAD, "").getValue();
                }
                break;
            case ApplicationConstants.BROWSER_MSIE_ALT:
                if (userAgent.contains("Win64") || userAgent.contains("IA64") || userAgent.contains("x64") || userAgent.contains("WOW64")) {
                    downloadUrl = dynamicProperty.getStringProp(
                            ApplicationConstants.BROWSER_IE_64_EXTENSION_DOWNLOAD, "").getValue();
                } else {
                    downloadUrl = dynamicProperty.getStringProp(
                            ApplicationConstants.BROWSER_IE_32_EXTENSION_DOWNLOAD, "").getValue();
                }
                break;
            default :
                downloadUrl = dynamicProperty.getStringProp(
                        ApplicationConstants.BROWSER_CHROME_EXTENSION_DOWNLOAD, "").getValue();
        }

        model.addAttribute(ApplicationConstants.DOWNLOAD_URL, downloadUrl);
        try{
            String version = addonInfoService.getAddonInfo((AcHostModel) hostUser.getHost()).getVersion();
            model.addAttribute("captureVersion", version);
        } catch (Exception exception){
            log.warn("Error during getting addon info.", exception);
            model.addAttribute("captureVersion", "n/a");
        }

        return "get-browser-extension";
    }
}
