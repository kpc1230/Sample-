package com.thed.zephyr.capture.service.job;

import com.thed.zephyr.capture.service.PingHomeService;
import com.thed.zephyr.capture.service.data.BackUpService;
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

import java.text.SimpleDateFormat;
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
    private BackUpService backUpService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                Boolean pingAvailability = dynamicProperty.getBoolProp(ApplicationConstants.DIAL_HOME_JOB, false).get();
                if (pingAvailability) {
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
        // Added daily backup job
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
                Boolean dailyBackupJob = dynamicProperty.getBoolProp(ApplicationConstants.DAILY_BACKUP_JOB_ENABLE, false).get();
                if (dailyBackupJob) {
                    log.info("Start daily backup job ...");
                    long startTime = System.currentTimeMillis();
                    backUpService.runDailyBackupJob();
                } else {
                    log.info("Daily backup job is disabled, please check in dynamic.prop.conf app.daily.backup.job property.");
                }

            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                String cron = dynamicProperty.getStringProp(ApplicationConstants.DAILY_BACKUP_JOB_CRON_EXP,
                        ApplicationConstants.DAILY_BACKUP_JOB_DEFAULT_CRON_EXPR).get();
                CronTrigger trigger = new CronTrigger(cron);
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                log.info("The next time dailyBackupJob job will be triggered:{}", dt.format(nextExec));
                return nextExec;
            }
        });
    }
}
