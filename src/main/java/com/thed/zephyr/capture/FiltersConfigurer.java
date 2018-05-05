package com.thed.zephyr.capture;

import com.thed.zephyr.capture.filter.EntranceInterceptor;
import com.thed.zephyr.capture.filter.MaintenanceInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Masud on 4/16/18.
 */
@Configuration
public class FiltersConfigurer
        extends WebMvcConfigurerAdapter {

    @Autowired
    private EntranceInterceptor entranceInterceptor;
    @Autowired
    private MaintenanceInterceptor maintenanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(entranceInterceptor);
        registry.addInterceptor(maintenanceInterceptor);
    }
}
