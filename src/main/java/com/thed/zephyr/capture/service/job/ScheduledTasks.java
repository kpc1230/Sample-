package com.thed.zephyr.capture.service.job;

import com.thed.zephyr.capture.service.PingHomeService;
import com.thed.zephyr.capture.service.TenantUpdateService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Masud on 9/17/17.
 */

@EnableScheduling
@Component
public class ScheduledTasks implements SchedulingConfigurer {

    @Autowired
    private Logger log;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private PingHomeService pingHomeService;

    @Autowired
    private TenantUpdateService tenantUpdateService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        //Dial Ping Home
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                Boolean pingAvailability = dynamicProperty.getBoolProp(ApplicationConstants.DIAL_HOME_JOB, false).get();
                if(pingAvailability){
                    log.info("Start ping home job ...");
                    pingHomeService.runPing();
                } else {
                    log.info("Ping job home disabled, please check in dynamic.prop.conf app.dial.home.job property.");
                }

            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cron = dynamicProperty.getStringProp(ApplicationConstants.VERSION_PING_CRON_EXPR,
                        ApplicationConstants.VERSION_PING_DEFAULT_CRON_EXPR).get();
                log.info("The next time pingVersionStatus job will be triggered:{}", cron.toString());
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        });

        //Tenant Status Update
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                Boolean runTenantStatus = dynamicProperty.getBoolProp(ApplicationConstants.TENANT_STATUS_UPDATE_JOB, true).get();
                if(runTenantStatus){
                    log.info("Start pulling tenant status job ...");
                    tenantUpdateService.runAllTenantStatusUpdate();
                } else {
                    log.info("Tenant status update job disabled, please check in dynamic.prop.conf tenant.status.update.job property.");
                }

            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cron = dynamicProperty.getStringProp(ApplicationConstants.TENANT_STATUS_UPDATE_CRON,
                        ApplicationConstants.TENANT_STATUS_UPDATE_CRON_EXPR).get();
                log.info("The next time tenant status update job will be triggered:{}", cron.toString());
                CronTrigger trigger = new CronTrigger(cron);
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        });
    }
}
