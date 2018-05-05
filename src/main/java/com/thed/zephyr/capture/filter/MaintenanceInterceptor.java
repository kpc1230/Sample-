package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.impl.TenantAwareCacheWrapper;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MaintenanceInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Logger log;
    @Autowired
    DynamicProperty dynamicProperty;
    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private TenantAwareCacheWrapper tenantAwareCache;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Boolean isMaintenanceActive = dynamicProperty.getBoolProp(ApplicationConstants.MAINTENANCE_ACTIVE,false).getValue();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isMaintenanceActive){
            return true;
        } else if(auth != null && auth.getPrincipal() instanceof AtlassianHostUser){
            return checkIfMaintenanceDoneByTenant((AtlassianHostUser)auth.getPrincipal(), response);
        }
        addMaintenanceMessageToResponse(null, response);

        return false;
    }

    private boolean checkIfMaintenanceDoneByTenant(AtlassianHostUser hostUser, HttpServletResponse response){
        String ctId = ((AcHostModel)hostUser.getHost()).getCtId();
        IList<Object> list = hazelcastInstance.getList(ApplicationConstants.MAINTENANCE_DONE_LIST);
        if(list.contains(ctId)){
            return true;
        }
        addMaintenanceMessageToResponse(hostUser, response);

        return false;
    }

    private void addMaintenanceMessageToResponse(AtlassianHostUser hostUser, HttpServletResponse response){
        String content = null;
        if(hostUser != null){
            content = CaptureUtil.readHtmlTemplate("classpath:\\templates\\maintenanceMessage.html", hostUser.getHost().getBaseUrl(), resourceLoader);
        }
        content = content != null?content:"We are performing a regular maintenance on our Capture Cloud servers. We will be up soon.";
        try {
            response.getWriter().write(content);
        } catch (IOException exception) {
            log.error("Error during adding maintenance message into server response", exception);
        }
    }
}
