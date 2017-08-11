package com.thed.zephyr.capture.connect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;

/**
 * Created by aliakseimatsarski on 10/20/16.
 */

@Component
public class ConnectSigningRestTemplate extends RestTemplate {

    @Autowired
    public ConnectSigningRestTemplate(ConnectRequestInterceptor requestInterceptor) {
        setInterceptors(singletonList(requestInterceptor));
    }
}
