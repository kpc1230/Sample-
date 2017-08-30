package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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

    public static String getCurrentCtId(DynamoDBAcHostRepository dynamoDBAcHostRepository){
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findOne(atlassianHostUser.getHost().getClientKey());
        return acHostModel.getCtId();
    }

    /**
     * Creates the page request object for pagination.
     *
     * @param offset -- Offset position to start
     * @param limit -- Number of records to return
     * @return -- Returns the page request object.
     */
    public static PageRequest getPageRequest(Integer offset, Integer limit) {
        return new PageRequest((Objects.isNull(offset) ? 0 : offset), (Objects.isNull(limit) ? ApplicationConstants.DEFAULT_RESULT_SIZE : limit));
    }


    /**
     * There are two cases, remote icon or within jira. The remote icon can be returned as is while the jira one will need the baseURL added to the
     * front
     *
     * @param issue issue
     * @param host
     * @return full url to the image
     */
    public static String getFullIconUrl(Issue issue, AtlassianHostUser host) {
        return getFullIconUrl(issue.getIssueType(),host.getHost().getBaseUrl());
    }


    public static String getFullIconUrl(IssueType it, String baseUrl) {
        String iconUrl = it.getIconUri().toString();
        String imgSrc = "";
        if (iconUrl.indexOf("http") >= 0) {
            // In the case of remote issue icons
            imgSrc = iconUrl.substring(iconUrl.indexOf("http"));
        } else {
            if (!StringUtils.isEmpty(iconUrl)) {
                imgSrc = baseUrl + iconUrl;
            } else {
                // To prevent empty img tags
                imgSrc = baseUrl + "/images/icons/undefined.gif";
            }
        }
        return imgSrc;
    }
}
