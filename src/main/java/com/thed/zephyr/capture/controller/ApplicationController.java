package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
import com.fasterxml.jackson.databind.JsonNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.annotation.LicenseCheck;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.UniqueIdGenerator;

import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by snurulla on 8/16/2017.
 */
@Controller
public class ApplicationController {

    @Autowired
    private Logger log;
    @Autowired
    private UserService jiraUserService;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private Environment env;
    @Autowired
    private CaptureI18NMessageSource i18n;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private AddonInfoService addonInfoService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @LicenseCheck
    @RequestMapping(value = "/adminGenConf")
    public String getGeneralConfigurationPage(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                              @RequestParam String user_id, Model model) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the general configuration page with username : " + user_id);
        JsonNode jsonNode = addonInfoService.getProperty(acHostModel, "captureGenPageSettings");
        JsonNode resp = null;
        if (jsonNode != null) {
            resp = jsonNode.get("value");
        }
        model.addAttribute("generalConfigData", resp);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());


        log.debug("Ending Requesting the general configuration page with resp : " + jsonNode);
        return "generalConfigPage";
    }

    @LicenseCheck
    @RequestMapping(value = "/browseTestSessions")
    public String getSessionNavigatorPage(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the Browse Test Sessions page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());

        log.debug("Ending Requesting the Browse Test Sessions page");
        return "sessionNavigator";
    }

    @LicenseCheck
    @RequestMapping(value = "/viewSession")
    public String getViewSessionPage(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the Session Navigator page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());

        log.debug("Ending Requesting the Session Navigator page");
        return "viewSession";
    }

    @LicenseCheck
    @RequestMapping(value = "/public/rest/testing")
    public String getTestingIssueView(@RequestParam String projectId, @RequestParam String projectKey, @RequestParam String issueId, @RequestParam String issueKey, @RequestParam String boardsPage, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the Testing Issue View page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("issueId", issueId);
        model.addAttribute("issueKey", issueKey);
        model.addAttribute("boardsPage", boardsPage);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());

        log.debug("Ending Requesting the Testing Issue View page");
        return "testingIssueView";
    }

    @LicenseCheck
    @RequestMapping(value = "/projectTestSessions")
    public String projectTestSessions(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the Project Test Sessions page");
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());

        log.debug("Ending Requesting the Project Test Sessions page");
        return "projectTestSessions";
    }

    @LicenseCheck
    @RequestMapping(value = "/createTestSessionDialog")
    public String createTestSessionDialog(@RequestParam String projectId, @RequestParam String projectKey, @RequestParam String issueId, @RequestParam String issueKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        log.debug("Requesting the Project Test Sessions page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("pluginKey", pluginKey);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("issueId", issueId);
        model.addAttribute("issueKey", issueKey);
        model.addAttribute("messages", getI18NMessagesBasedOnSessionLocale());

        log.debug("Ending Requesting the Project Test Sessions page");
        return "createTestSessionDialog";
    }

    @LicenseCheck
    @RequestMapping(value = "/wikiHelp")
    public String wikiHelp() {
        log.debug("Requesting the wiki help page");
        return "wikiHelp";
    }

    @IgnoreJwt
    @RequestMapping(value = "/capture-i18n")
    @ResponseBody
    public ResponseEntity<?> getI18NMessages() {
        Map<String, String> messages = getI18NMessagesBasedOnSessionLocale();
        return ResponseEntity.ok(messages);
    }
    
    @IgnoreJwt
    @RequestMapping(value = "/clearCache")
    @ResponseBody
    public ResponseEntity<?> clearCache(@AuthenticationPrincipal AtlassianHostUser hostUser) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        tenantAwareCache.clearTenantCache(acHostModel);
        Map<String,String> map = new HashedMap();
        map.put("status","success");
        return ResponseEntity.ok(map);
    }

    /* THIS METHOD SHOULD BE DELETED AFTER ATLASSIAN MIGRATION WILL BE DONE!!!!!!!*/
    @IgnoreJwt
    @RequestMapping(value = "/private/clear/achost/form/cache", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<?> deleteAcHostFromCache(@RequestParam("clientKey") String clientKey) {
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        tenants.delete(clientKey);
        Map<String,String> map = new HashedMap();
        map.put("status","success");
        log.info("The AcHost was deleted from cache clientKey:{}", clientKey);
        return ResponseEntity.ok(map);
    }
    
    @IgnoreJwt
    @PostMapping(value = "/reindex")
    public ResponseEntity<?> reindex(@AuthenticationPrincipal AtlassianHostUser hostUser) {
    	try {
    		log.info("Start of reindex()");
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            sessionService.reindexSessionDataIntoES(acHostModel, jobProgressId, acHostModel.getCtId());
            log.info("End of reindex()");
            Map<String, String> response = new HashMap<>();
            response.put("jobProgressId", jobProgressId);
            return ResponseEntity.ok(response);
    	} catch(Exception ex) {
    		log.error("Erro in reindex() -> ", ex);
    		throw new CaptureRuntimeException(ex);
    	}
    }
    
    @GetMapping(value = "/checkBESupportedVersion")
    @IgnoreJwt
    public ResponseEntity<?> checkBESupportedVersion(@RequestParam String currentBEVersion) {
    	log.info("Start of checkBESupportedVersion() --> params - " + currentBEVersion);
    	String supportedBEversion = dynamicProperty.getStringProp("current.supported.be.version", "3.0.0.0").get();
    	boolean flag = false;
    	if(supportedBEversion.trim().equals(currentBEVersion)) {
    		flag = true;
    	}
    	Map<String, Object> response = new HashMap<>();
    	response.put("result", flag);
    	log.info("End of checkBESupportedVersion()");
    	return ResponseEntity.ok(response);
    }

    private Map<String, String> getI18NMessagesBasedOnSessionLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        String basename = "i18n/capture-i18n";
        return i18n.getKeyValues(basename, locale);
    }
}
