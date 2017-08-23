package com.thed.zephyr.capture;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.netflix.config.DynamicPropertyFactory;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.addon.impl.AddonInfoServiceImpl;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepositoryImpl;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.servlet.Filter;


@SpringBootApplication
@EnableCaching
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

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public AtlassianHostRepository atlassianHostRepository(){
        return new DynamoDBAcHostRepositoryImpl();
    }


    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Primary
    public HazelcastInstance hazelcastInstance() {
         return Hazelcast.newHazelcastInstance();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    CacheManager cacheManager() {
        return new HazelcastCacheManager(hazelcastInstance());
    }

    @Bean
    public FilterRegistrationBean registration(@Qualifier("jwtAuthenticationFilter") Filter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
}
