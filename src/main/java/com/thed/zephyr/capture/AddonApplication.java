package com.thed.zephyr.capture;
import org.springframework.core.env.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.addon.impl.AddonInfoServiceImpl;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
@SpringBootApplication
public class AddonApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplication(AddonApplication.class).run(args);
    }

    @Autowired
    private Environment env;


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Logger logger(){
        return LoggerFactory.getLogger("application");
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public AddonInfoService addonInfoService(){
        return new AddonInfoServiceImpl();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DynamicPropertyFactory dynamicPropertyFactory(){
        String configFileName = env.getProperty(ApplicationConstants.DYNAMIC_PROPERTY_CONFIG_FILE);
        if (StringUtils.isNotBlank(configFileName)){
            System.setProperty("archaius.configurationSource.defaultFileName", configFileName);
        }
        String configFileURLs = env.getProperty(ApplicationConstants.DYNAMIC_PROPERTY_CONFIG_URLS);
        if (StringUtils.isNotBlank(configFileURLs)){
            System.setProperty("archaius.configurationSource.additionalUrls", configFileURLs);
        }

        return DynamicPropertyFactory.getInstance();
    }
}
