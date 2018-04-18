package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.annotation.LicenseCheck;
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;

@Component
public class LicenseCheckHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private Logger log;
    @Autowired
    DynamicProperty dynamicProperty;
    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Boolean forceLicenseCheck = dynamicProperty.getBoolProp(ApplicationConstants.FORCE_LICENSE_CHECK, true).get();
        if (forceLicenseCheck && !handlerRequiresLicense(handler) && !licenseCheck(request)) {
            String errorContent = errorContent();
            response.getWriter().write(errorContent);
            return false;
        }
        return true;
    }

    private boolean handlerRequiresLicense(Object handler){
        return handler instanceof HandlerMethod && !handlerHasAnnotation((HandlerMethod) handler, LicenseCheck.class);
    }


    private <T extends Annotation> boolean handlerHasAnnotation(HandlerMethod method, Class<T> annotationClass) {
        return method.getMethod().isAnnotationPresent(annotationClass)
                || method.getBeanType().isAnnotationPresent(annotationClass);
    }

    private boolean licenseCheck(HttpServletRequest request){
        String[] lics = request.getParameterMap().get("lic");
        String license = lics != null?lics[0]:"";

        return StringUtils.equals(license, "active");
    }

    private String errorContent(){
        String content = "";
        try{
            Resource resource = resourceLoader.getResource("classpath:\\templates\\error403.html");
            InputStream inputStream = resource.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer);
            content = writer.toString();
            String baseUrl = getPrincipal().getHost().getBaseUrl();
            content = StringUtils.replace(content, "{base-url}", baseUrl);
        } catch (Exception exception){
            log.error("Error during get error403.html content.", exception);
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
