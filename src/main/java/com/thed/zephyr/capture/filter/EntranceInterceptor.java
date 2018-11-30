package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.gdpr.UserAccountActivitesService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Masud on 4/16/18.
 */
@Component
public class EntranceInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Logger log;

    @Autowired
    DynamicProperty dynamicProperty;

    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    private UserAccountActivitesService userAccountActivitesService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Boolean passRequest = false;
            //check entrance allowed flag
            Boolean isAllowed = dynamicProperty.getBoolProp(ApplicationConstants.ENTRANCE_CHECKING,false).getValue();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth != null && isAllowed){
                if(auth.getPrincipal() instanceof String && auth.getPrincipal().toString().equals("anonymousUser")){
                    log.info("AnnonymousUser trying to access.");
                    return true;
                }else{
                    try {
                        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

                        List<String> getAllowedTenantList = dynamicProperty.getStringListProp(ApplicationConstants.ENTRANCE_CHECKING_LIST,new ArrayList<>()).getValue();

                        if(getAllowedTenantList != null && getAllowedTenantList.size()>0){
                            for(String clientKey: getAllowedTenantList){
                                if(clientKey.equals(host.getHost().getClientKey())) {
                                    passRequest = true;
                                    break;
                                }
                            }
                        }

                        if (!passRequest) {
                            String errorContent = errorContent();
                            response.getWriter().write(errorContent);
                            log.info("Checked tenant from dynamic property to deny them clientKey:{}",host.getHost().getClientKey());
                            return false;
                        }
                    }catch (ClassCastException ex){
                        log.error("error during getting jwt auth principal. {}",ex.getMessage());
                    }

            }
        }
        
        if(auth != null && dynamicProperty.getBoolProp("user.account.polling", false).get()) { 
        	try {
        		 AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
                 AcHostModel acHostModel = (AcHostModel) host.getHost();
                 //Saving user activities for gdpr reporting.
                 userAccountActivitesService.saveUserActivities(host.getUserAccountId().get(), acHostModel.getClientKey(),
                 		acHostModel.getCtId(), request.getRequestURL().toString());
        	} catch(Exception ex) {
        		log.error("error during saving user activites for gdpr reporting",ex.getMessage());
        	}
        }
        return true;
    }


    private String errorContent(){
        String content = "";
        try{
            Resource resource = resourceLoader.getResource("classpath:\\templates\\errorEntrance.html");
            InputStream inputStream = resource.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer);
            content = writer.toString();
            String baseUrl = getPrincipal().getHost().getBaseUrl();
            content = StringUtils.replace(content, "{base-url}", baseUrl);
        } catch (Exception exception){
            log.error("Error during get errorEntrance.html content.", exception);
        }

        return content;
    }

    private AtlassianHostUser getPrincipal() {
        boolean signed = false;
        AtlassianHostUser atlassianHostUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            atlassianHostUser = (AtlassianHostUser)authentication.getPrincipal();
        }

        return atlassianHostUser;
    }

}