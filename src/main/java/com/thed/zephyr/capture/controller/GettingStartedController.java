package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Masud on 8/15/17.
 */
@Controller
public class GettingStartedController {

    @Autowired
    private AddonInfoService addonInfoService;

    @RequestMapping(value = "/getting-started", method = RequestMethod.GET)
    public String gettingStartedPage(@AuthenticationPrincipal AtlassianHostUser hostUser, Model model){
        model.addAttribute("captureVersion",
                addonInfoService.getAddonInfo((AcHostModel) hostUser.getHost()).getVersion());
        return "getting-started";
    }
}
