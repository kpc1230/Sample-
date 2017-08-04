package com.atlassian.bonfire.service;

import com.atlassian.excalibur.web.util.ReflectionKit;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service(TimeZoneService.SERVICE)
public class TimeZoneServiceImpl implements TimeZoneService {
    public DateTimeZone getLoggedInUserTimeZone() {
        TimeZone userTimeZone = ReflectionKit.methodOfJIRAComponent("com.atlassian.jira.timezone.TimeZoneManager", "getLoggedInUserTimeZone", TimeZone.getDefault()).call();
        return DateTimeZone.forTimeZone(userTimeZone);
    }
}
