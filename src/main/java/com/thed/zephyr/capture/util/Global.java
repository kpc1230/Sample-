package com.thed.zephyr.capture.util;

import com.thed.zephyr.capture.model.AcHostModel;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by Masud on 8/16/17.
 */
@Component
public class Global {

    @Autowired
    private Logger log;

    @Bean
    public boolean onGlobalStart() {
        log.info("Global Started");

        //load or initialize something from here
        //customMethod()

        return true;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AcHostModel createAcHostModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null) {
            AcHostModel acHostModel = (AcHostModel) auth.getPrincipal();
            if (acHostModel != null) {
                return new AcHostModel(acHostModel);
            }
        }
        AcHostModel acHostModel1 = new AcHostModel();
        acHostModel1.setBaseUrl("http://unknownHost/jira");
        return acHostModel1;
    }
}
