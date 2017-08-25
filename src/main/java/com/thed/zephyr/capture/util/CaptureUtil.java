package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import org.apache.http.NameValuePair;
import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CaptureUtil {

    private static final Logger log = LoggerFactory.getLogger("application");

    public static String base64(String str) {
        byte[] encodedBytes = Base64.encodeBase64(str.getBytes());
        return new String(encodedBytes);
    }


    public static String decodeBase64(String str) {
        byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(str);
        return new String(decodedBytes);
    }

    public static Long getHourBeginning(){
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Long hourStart = dateTime.withHourOfDay(dateTime.getHourOfDay())
                .withMinuteOfHour(0).withSecondOfMinute(0)
                .withMillisOfSecond(0).getMillis();

        return hourStart;
    }

    public static Long getDayBeginning(){
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Long dayStart = dateTime.withTimeAtStartOfDay().getMillis();

        return dayStart;
    }

    public static Long getWeekBeginning(){
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Long weekStart = dateTime.withDayOfWeek(1).withTimeAtStartOfDay().getMillis();

        return weekStart;
    }

    public static Long getMonthBeginning(){
        DateTime dateTime = new DateTime(DateTimeZone.UTC);
        Long monthStart = dateTime.withDayOfMonth(1).withTimeAtStartOfDay().getMillis();

        return monthStart;
    }

    public static String getLargeAvatarUrl(String userKey) {
        log.error("The method CaptureUtil.getLargeAvatarUrl() needs to be implemented!");
        return "";
    }

    public static void getParamMap(List<NameValuePair> params, Map<String, String[]> paramMap) {
        for (final NameValuePair nameValuePair : params) {
            final String name = nameValuePair.getName();
            final String value = nameValuePair.getValue();

            String[] array = new String[] { value};
            if(paramMap.containsKey(name)) {
                final String[] currentValues = paramMap.get(name);
                final int newLength = currentValues.length + 1;
                array = Arrays.copyOf(currentValues, newLength);
                array[newLength] = value;
            }
            paramMap.put(name, array);
        }
    }

    public static String getCurrentClientKey(){
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return atlassianHostUser.getHost().getClientKey();
    }
}
