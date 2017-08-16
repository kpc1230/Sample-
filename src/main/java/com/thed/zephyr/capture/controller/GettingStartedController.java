package com.thed.zephyr.capture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Masud on 8/15/17.
 */
@Controller
public class GettingStartedController {

    @RequestMapping(value = "/getting-started", method = RequestMethod.GET)
    public String gettingStartedPage(){
        return "getting-started";
    }
}
