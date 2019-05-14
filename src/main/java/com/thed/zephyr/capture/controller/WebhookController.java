package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.gdpr.MigrateService;
import com.thed.zephyr.capture.service.jira.IssueLinkTypeService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class WebhookController {

    @Autowired
    private Logger log;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;
    @Autowired
    private AddonInfoService addonInfoService;
    @Autowired
    private Environment env;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    IssueLinkTypeService issueLinkTypeService;
    @Autowired
    private MigrateService migrateService;

    @RequestMapping(value = "/rest/event/plugin/enabled", method = RequestMethod.POST)
    public ResponseEntity pluginEnableEvent(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Plugin enable event for tenantId:{}", tenantId);
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(hostUser.getHost().getClientKey());
            acHostModel.setStatus(AcHostModel.TenantStatus.ACTIVE);
            acHostModel.setCreatedByAccountId(hostUser.getUserAccountId().get());
            atlassianHostRepository.save(acHostModel);

            if(acHostModel != null && !CaptureUtil.isTenantGDPRComplaint()) {
                String jobProgressId = new UniqueIdGenerator().getStringId();
                migrateService.migrateData(hostUser, acHostModel, jobProgressId);
            }
        } catch (Exception exception) {
            log.error("Error during plugin enable event.", exception);
        }

        //creating capture custom link type while add on enable
        issueLinkTypeService.createIssuelinkTypeCaptureTestingIfNotExist(hostUser);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/rest/event/plugin/disabled", method = RequestMethod.POST)
    public ResponseEntity pluginDisabledEvent(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Plugin disabled event for tenantId:{}", tenantId);
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(hostUser.getHost().getClientKey());
            acHostModel.setStatus(AcHostModel.TenantStatus.PLUGIN_DISABLED);
            atlassianHostRepository.save(acHostModel);
        } catch (Exception exception) {
            log.error("Error during plugin enable event.", exception);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/rest/event/comment/created", method = RequestMethod.POST)
    public ResponseEntity commentCreated(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Add comment event for tenantId:{}", tenantId);
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(hostUser.getHost().getClientKey());
            if(acHostModel != null && !CaptureUtil.isTenantGDPRComplaint()) {
                String jobProgressId = new UniqueIdGenerator().getStringId();
                migrateService.migrateData(hostUser, acHostModel, jobProgressId);
            }
        } catch (Exception exception) {
            log.error("Error during add comment event. {}", exception.getMessage());
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/rest/event/comment/updated", method = RequestMethod.POST)
    public ResponseEntity commentUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Update comment event for tenantId:{}", tenantId);
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(hostUser.getHost().getClientKey());
            if(acHostModel != null && !CaptureUtil.isTenantGDPRComplaint()) {
                String jobProgressId = new UniqueIdGenerator().getStringId();
                migrateService.migrateData(hostUser, acHostModel, jobProgressId);
            }
        } catch (Exception exception) {
            log.error("Error during Update comment event. {}", exception.getMessage());
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
