package com.thed.zephyr.capture.util;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Tag;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.tomcat.util.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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

    public static String getCurrentClientBaseUrl(){
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        return host.getHost().getBaseUrl();
    }
    public static String getCurrentCtId(DynamoDBAcHostRepository dynamoDBAcHostRepository){
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findOne(atlassianHostUser.getHost().getClientKey());
        return acHostModel.getCtId();
    }
    
    public static AcHostModel getAcHostModel(DynamoDBAcHostRepository dynamoDBAcHostRepository) {
        AtlassianHostUser atlassianHostUser = (AtlassianHostUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findOne(atlassianHostUser.getHost().getClientKey());
        return acHostModel;
    }
    
    public static AcHostModel getAcHostModel(DynamoDBAcHostRepository dynamoDBAcHostRepository, String baseUrl) {
        AcHostModel acHostModel = (AcHostModel) dynamoDBAcHostRepository.findFirstByBaseUrl(baseUrl).orElse(null);
        return acHostModel;
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

    public static String getOSIcon(String os) {
        if (StringUtils.isNotBlank(os)) {
            if (os.toLowerCase().contains(CaptureConstants.OS_LINUX)) {
                return CaptureConstants.OS_LINUX;
            } else if (os.toLowerCase().contains(CaptureConstants.OS_WINDOWS)) {
                return CaptureConstants.OS_WINDOWS;
            } else if (os.toLowerCase().contains(CaptureConstants.OS_MAC)) {
                return CaptureConstants.OS_MAC.trim();// yeah osx
            }
        }
        return "none";
    }

    public static String getBrowserIcon(String browser) {
        if (StringUtils.isNotBlank(browser)) {
            if (browser.toLowerCase().contains(CaptureConstants.BROWSER_FIREFOX)) {
                return CaptureConstants.BROWSER_FIREFOX;
            } else if (browser.toLowerCase().contains(CaptureConstants.BROWSER_MSIE) || browser.toLowerCase().contains(CaptureConstants.BROWSER_MSIE_ALT)) {
                return CaptureConstants.BROWSER_MSIE;
            } else if (browser.toLowerCase().contains(CaptureConstants.BROWSER_CHROME)) {
                return CaptureConstants.BROWSER_CHROME;
            } else if (browser.toLowerCase().contains(CaptureConstants.BROWSER_SAFARI)) {
                return CaptureConstants.BROWSER_SAFARI;
            }
        }
        return "none";
    }
    public static Set<String> parseTagsAsSet(String noteData) {
    	List<String> tagList = CaptureUtil.parseTags(noteData);
		Set<String> tags = null;
		if(tagList != null && !tagList.isEmpty()){
			tags = tagList.stream().collect(Collectors.toSet());
		}else{
			tags = new TreeSet<>();
		}
		return tags;
    }
    public static List<String> parseTags(String noteData) {
		List<String> tagList = new ArrayList<>();
		Pattern pattern = Pattern.compile("#(\\w+)|#!|#\\?");
		Matcher matcher = pattern.matcher(noteData);
		String tagName;
		while (matcher.find()) {
			String originalMatch = matcher.group(0);
			if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.QUESTION)){
				tagName = Tag.QUESTION_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.FOLLOWUP)){
				tagName = Tag.FOLLOWUP_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.ASSUMPTION)){
				tagName = Tag.ASSUMPTION_TAG_NAME;
			} else if (org.apache.commons.lang3.StringUtils.equals(originalMatch, Tag.IDEA)){
				tagName = Tag.IDEA_TAG_NAME;
			} else {
				tagName = matcher.group(1);
			}

			tagList.add(tagName);
		}

		return tagList;
	}

    public static String createWikiData(WikiParser wikiParser, String rawData){
        String convStr = null;
        if(StringUtils.isNotEmpty(rawData)){
            convStr = wikiParser.parseWiki(rawData, ApplicationConstants.HTML);
            convStr = createNoteData(convStr);
        }
    	return convStr;
    }
	public static String createNoteData(String noteData) {
//		StringBuilder data = new StringBuilder();
//		String[] lines = noteData.split("\\r?\\n");
//		boolean isPrevNewLine = true;
//		boolean isFirstLine = true;
//		for(String line : lines) {
//			switch(line.trim()) {
//				case "": {
//					if(isPrevNewLine) {
//						isPrevNewLine = false;
//						data.append("");
//					}
//					break;
//				} default: {
//					if(!isFirstLine && isPrevNewLine) {
//						data.append("<br/>");
//					} else {
//						data.append("");
//					}
//					data.append(getAtlassiaonWikiformatted(line));
//					isFirstLine = false;
//					isPrevNewLine = true;
//				}
//			}
//		}
//		data.append("");
		return StringUtils.isNotEmpty(noteData)?getAtlassiaonWikiformatted(noteData):noteData;
	}
	
	private static String getAtlassiaonWikiformatted(String line) {
		StringBuilder finalData = new StringBuilder();
		String[] words = line.split("[ ]+");
		for(String word : words) {
			String tagName = null;
			String cssClass = null;
			if(word.length() >= 2) { //replacing if start tag is there.
				String startTag = word.substring(0, 2);
				switch(startTag) {
					case Tag.QUESTION: 
						tagName = Tag.QUESTION_TAG_NAME;
						cssClass = "tag-question";
						break;
					case Tag.FOLLOWUP:
						cssClass = "tag-followUp";
						tagName = Tag.FOLLOWUP_TAG_NAME;
						break;
					case Tag.ASSUMPTION: 
						tagName = Tag.ASSUMPTION_TAG_NAME;
						cssClass = "tag-assumption";
						break;
					case Tag.IDEA:
						cssClass = "tag-idea";
						tagName = Tag.IDEA_TAG_NAME;	
				}
			}
			String target = String.format(" target=\"".concat("%s")+"\"","_parent");
			if(Objects.nonNull(tagName) && Objects.nonNull(cssClass)) {
				finalData.append(" <span class=\"note-tag ").append(cssClass).append("\"></span>").append(word.length() > 2 ? word.substring(2) : "");
			} else {
				if(word.matches("^(https|http|ftp|ftps)://(.)+$")) {
					finalData.append(" <a ".concat(target)+" href=\"").append(word).append("\">").append(word).append("</a>");
				} else {
					finalData.append(" " + word);
				}
			}
		}
		return finalData.toString().replace("\n","<br />");
	}
	
	private static String getTagData(String noteData, String tag) {
		int beginIndex = noteData.indexOf(tag);
		if (beginIndex == -1)
			return null;
		int endIndex = noteData.indexOf(Tag.HASH, beginIndex + 1);
		if (endIndex == -1) {
			return noteData.substring(beginIndex + tag.length());
		}
		return noteData.substring(beginIndex + tag.length(), endIndex);
	}

	public static String createSessionLink(String sessionId){
		return String.format(ApplicationConstants.SESSION_URL_TEMPLATE, sessionId);
	}
    public  static String createADGFlagCacheKey(String userKey){
        return String.valueOf(ApplicationConstants.ADG3_FLAG_CACHE_PREFIX + "_" + userKey);
    }
}
