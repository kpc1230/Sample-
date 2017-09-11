package com.thed.zephyr.capture.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GadgetController {
    @Autowired
    private Logger log;

    @RequestMapping(value = "/public/html/gadget")
    public String renderGagdets(@RequestParam String dashboardView, @RequestParam String dashboardId, @RequestParam String dashboardItem, @RequestParam String gadgetType, @RequestParam String xdm_e, @RequestParam String cp,Model model) {
        model.addAttribute("dashboardView",dashboardView);
        model.addAttribute("dashboardId",dashboardId);
        model.addAttribute("dashboardItem",dashboardItem);
        model.addAttribute("gadgetType",gadgetType);
        model.addAttribute("xdm_e",xdm_e);
        model.addAttribute("cp",cp);
        return "gadgets";
    }
}
