package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.UserAgentSniffer;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DynamicProperty dynamicProperty;

    @RequestMapping(value = "/get-browser-extension", method = RequestMethod.GET)
    public String getExtensionPage(HttpServletRequest request, Model model){
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
                downloadUrl = dynamicProperty.getStringProp(
                        ApplicationConstants.BROWSER_IE_EXTENSION_DOWNLOAD, "").getValue();
                break;

            default :
                downloadUrl = dynamicProperty.getStringProp(
                        ApplicationConstants.BROWSER_CHROME_EXTENSION_DOWNLOAD, "").getValue();
        }

        model.addAttribute(ApplicationConstants.DOWNLOAD_URL, downloadUrl);
        return "get-browser-extension";
    }
}
