package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.IgnoreJwt;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by aliakseimatsarski on 9/8/17.
 */
@RestController
public class AdminController {

    @Autowired
    private Logger log;

    @IgnoreJwt
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping(){
        return "pong";
    }
}
