package com.thed.zephyr.capture.web.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Most User Agent sniffers try to match known agents, we don't want this because that means we have to keep it up to date. Instead I have written
 * this parser like thing that tries to work things out from the user agent by matching known patterns. It isn't perfect. In theory we only need to
 * match for the supported OS/browsers but we want to make sure this class works for as many cases as possible, but we won't bend backwards except for
 * MSIE. Therefore this class is hammered by unit tests.
 */
public class UserAgentSniffer {
    /*
     * Dictionaries below
     */

    // These words appear in the user agent, but they really lie, not really the browser
    private static final List<String> BROWSER_BLACKLIST = Lists.newArrayList("Ubuntu", "AppleWebKit", "Gecko", "Presto", "Mobile", "Trident");
    // These browsers are correct but are inferior to other strings and are overridden if those other strings appear
    private static final List<String> BROWSER_INFERIOR_T1 = Lists.newArrayList("Mozilla", "Chromium");
    // These browsers are often overridden too but are superior to the tier 1 strings
    private static final List<String> BROWSER_INFERIOR_T2 = Lists.newArrayList("Chrome", "Safari", "Firefox");
    // These words appear in the user agent frequently but will never be the OS
    private static final List<String> OS_BLACKLIST = Lists.newArrayList("N", "U", "I", "compatible", "WOW64");
    // These strings normally indicate that this isn't the OS
    private static final List<String> OS_UNLIKELY = Lists.newArrayList("-", ":", "MSIE", "Gecko", ".NET", "Trident/");
    // These strings are inferior and should never replace an existing string
    private static final List<String> OS_INFERIOR = Lists.newArrayList("Macintosh", "X11");
    // These sub-strings are superior and normally mean we have found what we want - space character is important
    private static final List<String> OS_SUPERIOR = Lists.newArrayList("Linux ", "Windows ", "Mac OS X ");
    // Maps the OS string to the pretty OS name - If a translation can't be found then the raw os string is returned
    private static final Map<String, String> OS_PRETTY_NAME_TRANSLATIONS;

    static {
        Map<String, String> tempMap = Maps.newHashMap();
        // Windows first - http://msdn.microsoft.com/en-us/library/ms537503%28v=vs.85%29.aspx
        tempMap.put("Windows NT 6.3", "Windows 8.1");
        tempMap.put("Windows NT 6.2", "Windows 8");
        tempMap.put("Windows NT 6.1", "Windows 7");
        tempMap.put("Windows NT 6.0", "Windows Vista");
        tempMap.put("Windows NT 5.2", "Windows XP x64 Edition; Windows Server 2003");
        tempMap.put("Windows NT 5.1", "Windows XP");
        tempMap.put("Windows NT 5.01", "Windows 2000, Service Pack 1 (SP1)");
        tempMap.put("Windows NT 5.0", "Windows 2000");
        tempMap.put("Windows NT 4.0", "Microsoft Windows NT 4.0");
        tempMap.put("Windows 98", "Windows 98");
        tempMap.put("Windows 95", "Windows 95");
        tempMap.put("Windows CE", "Windows CE");
        // Then OSX - http://en.wikipedia.org/wiki/OS_X#Versions
        tempMap.put("10.0", "OS X Cheetah");
        tempMap.put("10.1", "OS X Puma");
        tempMap.put("10.2", "OS X Jaguar");
        tempMap.put("10.3", "OS X Panther");
        tempMap.put("10.4", "OS X Tiger");
        tempMap.put("10.5", "OS X Leopard");
        tempMap.put("10.6", "OS X Snow Leopard");
        tempMap.put("10.7", "OS X Lion");
        tempMap.put("10.8", "OS X Mountain Lion");
        OS_PRETTY_NAME_TRANSLATIONS = Collections.unmodifiableMap(tempMap);
    }

    private static final String MSIE = "MSIE";
    private static final String MSIE_ALT = "Trident/"; // IE11 browser has name Mozilla and can be identified by the keyword
    private static final String OSX_OS = "Intel Mac OS X";
    private static final String LIKE_OSX = "like Mac OS X"; // This is used by iOS devices. iPads don't show they are iOS devices
    private static final String EXPLICIT_VERSION = "Version";
    private static final String UBUNTU = "Ubuntu";
    private static final String SYMBIANOS = "SymbianOS";
    private static final String NOKIA = "Nokia";

    public static SniffedOS sniffOS(String userAgent) {
        String OS = "";

        String[] stage1 = tokenPara(userAgent);
        for (int i = 0; i != stage1.length; i++) {
            // We want to look in the parentheses, so we check for odd numbers
            if (i % 2 == 1) {
                String[] stage2 = tokenSemiColin(stage1[i]);
                for (int j = 0; j != stage2.length; j++) {
                    if (StringUtils.isNotBlank(stage2[j])) {
                        String incoming = stage2[j].trim();
                        if (isValidOS(OS, incoming)) {
                            OS = incoming;
                        }
                    }
                }
            }
        }
        String prettyName = translateToPretty(OS, userAgent);
        return new SniffedOS(OS, prettyName);
    }

    public static SniffedBrowser sniffBrowser(String userAgent) {
        String browser = "";
        String version = "";
        String explicitVersion = "";
        String[] stage1 = tokenPara(userAgent);
        for (int i = 0; i != stage1.length; i++) {
            // We don't want to look in the parentheses, so we check for even numbers
            if (i % 2 == 0) {
                String[] stage2 = tokenSpace(stage1[i]);
                for (int j = 0; j != stage2.length; j++) {
                    if (StringUtils.isNotBlank(stage2[j]) && stage2[j].indexOf("/") > 0) {
                        String[] split = stage2[j].split("/");
                        // Check that an explicit version is set
                        if (isExplicitVersion(split[0])) {
                            explicitVersion = split[1];
                        } else if (isValidBrowser(browser, split[0])) {
                            browser = split[0];
                            version = split[1];
                        }
                    }
                }
            }
        }
        // If we need to, then we search for MSIE
        if (shouldCheckMSIE(browser)) {
            for (int i = 0; i != stage1.length; i++) {
                // This time we want to look inside the parentheses, so we check for odd numbers
                if (i % 2 == 1) {
                    boolean msieWasDetected = false;
                    String[] stage2 = tokenSemiColin(stage1[i]);
                    for (int j = 0; j != stage2.length; j++) {
                        if (msieWasDetected) {
                            break;
                        }

                        if (StringUtils.isNotBlank(stage2[j]) && stage2[j].trim().indexOf(" ") > 0) {
                            String[] split = stage2[j].trim().split(" ");

                            if (isMSIE(split[0])) {
                                browser = split[0];
                                version = split[1];
                                msieWasDetected = true;
                            } else {
                                for (String theStage2Token : stage2) {
                                    // Iterates by each word of string and looks for "Trident/" and "rv:", e.g.:
                                    // Windows NT 6.3; Trident/7.0; rv:11.0
                                    // Windows NT 6.3; Trident/7.0; .NET4.0E; .NET4.0C; rv:11.0

                                    if (isNewMSIE(StringUtils.trim(theStage2Token))) {
                                        browser = "MSIE";
                                        // rv goes just before closing bracket
                                        version = StringUtils.substringAfter(stage2[stage2.length - 1], "rv:");

                                        msieWasDetected = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        // If an explicit version is set then we use that instead
        version = StringUtils.isBlank(explicitVersion) ? version : explicitVersion;
        return new SniffedBrowser(browser, version);
    }


    /**
     * Parses version from string "rv:11.0) like Gecko"
     *
     * @param browser
     * @return
     */
    private static String parseIE11version(String browser) {
        return StringUtils.substringBefore(StringUtils.substringAfter(browser, "rv:"), ") like");
    }

    /**
     * Split based on '(' or ')'
     */
    private static String[] tokenPara(String userAgent) {
        if (StringUtils.isNotBlank(userAgent)) {
            return userAgent.split("\\(|\\)");
        }
        return new String[0];
    }

    /**
     * Split based on ' '
     */
    private static String[] tokenSpace(String userAgent) {
        if (StringUtils.isNotBlank(userAgent)) {
            return userAgent.split(" ");
        }
        return new String[0];
    }

    /**
     * Split based on ';'
     */
    private static String[] tokenSemiColin(String userAgent) {
        if (StringUtils.isNotBlank(userAgent)) {
            return userAgent.split(";");
        }
        return new String[0];
    }

    /**
     * Checks to see if the incoming OS should replace the current one. Returns true if it should be replaced
     */
    private static boolean isValidOS(String current, String incoming) {
        if (OS_BLACKLIST.contains(incoming)) {
            return false;
        }
        // If it is less than 3 characters it probably isn't an OS - big assumption that will work for all known operating systems at time of writing
        // and gets rid of some language codes like 'en' 'fr'
        if (incoming.length() < 3) {
            return false;
        }
        // If the incoming string contains any unlikely characters then return false
        for (String s : OS_UNLIKELY) {
            if (incoming.contains(s)) {
                return false;
            }
        }
        // Remove anything that contains only numbers
        if (!Pattern.compile("[a-zA-Z]").matcher(incoming).find()) {
            return false;
        }
        // Don't replace current with incoming if incoming is inferior
        if (OS_INFERIOR.contains(incoming)) {
            return false;
        }
        // Don't replace current with incoming if current is superior
        for (String s : OS_SUPERIOR) {
            if (current.contains(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the incoming browser should replace the current one. Returns true if it should be replaced
     */
    private static boolean isValidBrowser(String current, String incoming) {
        if (BROWSER_BLACKLIST.contains(incoming)) {
            return false;
        }
        if (BROWSER_INFERIOR_T2.contains(incoming) && StringUtils.isNotBlank(current) && !BROWSER_INFERIOR_T1.contains(current)) {
            return false;
        }
        return true;
    }

    /**
     * Makes a best effort to translate the raw OS string into something more recognisable. The best effort result is returned.
     */
    private static String translateToPretty(String incoming, String userAgent) {
        // Translate the incoming string into it's pretty name
        String key = incoming;
        if (isOSX(incoming)) {
            key = extractOSXVersion(incoming);
        }
        String mappedValue = OS_PRETTY_NAME_TRANSLATIONS.get(key);
        String prettyName = StringUtils.isNotBlank(mappedValue) ? mappedValue : "";
        if (StringUtils.isBlank(prettyName)) {
            // see if this is an iOS device
            if (isIOS(incoming)) {
                String iDevice = extractIOSDevice(userAgent);
                if (StringUtils.isNotBlank(iDevice)) {
                    return iDevice;
                }
            }
            // If that didn't work, see if it is ubuntu
            if (userAgent.contains(UBUNTU)) {
                return UBUNTU;
            }
            // If that didn't work either see if it is a nokia
            if (incoming.contains(SYMBIANOS)) {
                return NOKIA;
            }
        }
        return prettyName;
    }

    private static boolean isIOS(String incoming) {
        return incoming.contains(LIKE_OSX);
    }

    private static String extractIOSDevice(String userAgent) {
        //Cut out the bit that is between the first '(' and the first ';'
        int beginIndex = userAgent.indexOf("(") + 1; // we don't want the '('
        int endIndex = userAgent.indexOf(";");
        if (beginIndex >= 0 && endIndex >= 0 && beginIndex < endIndex) {
            return userAgent.substring(beginIndex, endIndex);
        }
        return "";
    }

    private static String extractOSXVersion(String incoming) {
        String[] split = tokenSpace(incoming);
        if (split.length != 0) {
            for (int i = 0; i != split.length; i++) {
                if (Pattern.compile("[0-9]+((\\.|\\_)[0-9]+)*").matcher(split[i]).matches()) {
                    if (split[i].contains("_")) {
                        String[] digits = split[i].split("_");
                        return new StringBuffer().append(digits[0]).append(".").append(digits[1]).toString();
                    } else if (split[i].contains(".")) {
                        String[] digits = split[i].split("\\.");
                        return new StringBuffer().append(digits[0]).append(".").append(digits[1]).toString();
                    } else {
                        // Means it's just a number and we shd just return it
                        return split[i];
                    }
                }
            }
        }
        return "";
    }

    /**
     * Check to see if we should be checking for MSIE
     */
    private static boolean shouldCheckMSIE(String current) {
        return StringUtils.isBlank(current) || BROWSER_INFERIOR_T1.contains(current);
    }

    /**
     * Checks to see if the incoming string is an OSX OS String
     */
    private static boolean isOSX(String incoming) {
        return incoming.contains(OSX_OS);
    }

    /**
     * Checks to see if the incoming string is MSIE
     */
    private static boolean isMSIE(String incoming) {
        return MSIE.equals(incoming);
    }

    /**
     * Checks to see if the incoming string has "Trident/" id
     */
    private static boolean isNewMSIE(String incoming) {
        return StringUtils.startsWith(incoming, MSIE_ALT);
    }

    /**
     * If there is an explicit "Version" defined then that is probably the correct version
     */
    private static boolean isExplicitVersion(String incoming) {
        return EXPLICIT_VERSION.equals(incoming);
    }

    public static class SniffedBrowser {
        public String browser;
        public String version;

        public SniffedBrowser(String browser, String version) {
            this.browser = browser;
            this.version = version;
        }
    }

    public static class SniffedOS {
        public String OS;
        public String prettyName;

        public SniffedOS(String OS, String prettyName) {
            this.OS = OS;
            this.prettyName = prettyName;
        }
    }
}
