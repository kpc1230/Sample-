package com.thed.zephyr.capture.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;


public class CaptureUtil {

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
}
