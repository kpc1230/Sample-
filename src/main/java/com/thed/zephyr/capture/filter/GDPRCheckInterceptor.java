package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
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
import java.util.List;


@Component
public class GDPRCheckInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Logger log;

    @Autowired
    DynamicProperty dynamicProperty;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //check entrance allowed flag
        Boolean isGDPRForceCheck = dynamicProperty.getBoolProp(ApplicationConstants.ENABLE_GDPR_FORCE_CHECH, false).getValue();
        if (isGDPRForceCheck) {
            log.debug("enable.gdpr.force.check flag is enabled...");
            String skipGDPRForceCheckStr = request.getParameter("kcehCecroFRPDGpiks");
            Boolean skipGDPRForceCheck = skipGDPRForceCheckStr != null && skipGDPRForceCheckStr.equalsIgnoreCase("true");
            log.debug("skipGDPRForceCheck  :{}", skipGDPRForceCheck);
            if (!skipGDPRForceCheck) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    try {
                        if(auth.getPrincipal() instanceof String && auth.getPrincipal().toString().equals("anonymousUser")){
                            log.debug("Anonymous user: returning with true");
                            return true;
                        } else {
                            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
                            String baseUrl = host.getHost().getBaseUrl();
                            log.debug("Get tenant by baseURL:{} from DynamoDB", baseUrl);
                            List<AcHostModel> acHostModels = acHostModelRepository.findByBaseUrl(baseUrl);
                            AcHostModel acHostModel = null;
                            if (acHostModels.size() > 0) {
                                acHostModel = acHostModels.get(0);
                            }
                            Boolean skip = !AcHostModel.GDPRMigrationStatus.GDPR.equals(acHostModel.getMigrated()) && !AcHostModel.GDPRMigrationStatus.MIGRATED.equals(acHostModel.getMigrated());
                            if (acHostModel != null && AcHostModel.TenantStatus.ACTIVE.equals(acHostModel.getStatus()) && skip) {
                                String errorContent = errorContent();
                                response.setStatus(403);
                                response.getWriter().write(errorContent);
                                log.debug("Checked tenant from dynamic property to deny them clientKey:{}", host.getHost().getClientKey());
                                return false;
                            }
                        }
                    } catch (ClassCastException ex) {
                        log.error("error during getting jwt auth principal. {}", ex.getMessage());
                    }
                }

            }
        }
        return true;
    }


    private String errorContent() {
        String content = "";
        try {
            Resource resource = resourceLoader.getResource("classpath:\\templates\\errorGDPR.html");
            InputStream inputStream = resource.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer);
            content = writer.toString();
            String baseUrl = getPrincipal().getHost().getBaseUrl();
            content = StringUtils.replace(content, "{base-url}", baseUrl);
        } catch (Exception exception) {
            log.error("Error during get errorEntrance.html content.", exception);
        }

        return content;
    }

    private AtlassianHostUser getPrincipal() {
        boolean signed = false;
        AtlassianHostUser atlassianHostUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            atlassianHostUser = (AtlassianHostUser) authentication.getPrincipal();
        }

        return atlassianHostUser;
    }

}