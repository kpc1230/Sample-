package com.thed.zephyr.capture.service.jira.http;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Masud on 9/29/17.
 */
@Component
public class JwtRestTemplate extends RestTemplate{

    @Autowired
    private AtlassianHostRestClients restClients;

    public RestTemplate restTemplate(){
        return restClients.authenticatedAsAddon();
    }
}
