package com.thed.zephyr.capture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Masud on 12/12/17.
 */
@Controller
public class ZephyrProductsController {

    @RequestMapping(value = "/zephyr-products-info", method = RequestMethod.GET)
    public String getExtensionPage(String xdm_e, Model model){
        model.addAttribute("xdm_e",xdm_e);
        return "zephyr-products-info";
    }
}
