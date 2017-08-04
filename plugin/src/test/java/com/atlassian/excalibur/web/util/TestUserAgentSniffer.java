package com.atlassian.excalibur.web.util;

import com.atlassian.bonfire.web.util.UserAgentSniffer;
import com.atlassian.bonfire.web.util.UserAgentSniffer.SniffedBrowser;
import com.atlassian.bonfire.web.util.UserAgentSniffer.SniffedOS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUserAgentSniffer {
    @Test
    public void testUserAgentChromeOSX() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.11 Safari/535.19";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Chrome", browser.browser);
        assertEquals("18.0.1025.11", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10_6_8", os.OS);
        assertEquals("OS X Snow Leopard", os.prettyName);
    }

    @Test
    public void testUserAgentChromeWin7() {
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Chrome", browser.browser);
        assertEquals("20.0.1092.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 6.1", os.OS);
        assertEquals("Windows 7", os.prettyName);
    }

    @Test
    public void testUserAgentChromeLin() {
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Chrome", browser.browser);
        assertEquals("19.0.1084.9", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Linux x86_64", os.OS);
        assertEquals("", os.prettyName);

        userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Ubuntu/11.10 Chromium/18.0.1025.142 Chrome/18.0.1025.142 Safari/535.19";
        browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Chrome", browser.browser);
        assertEquals("18.0.1025.142", browser.version);
        os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Linux x86_64", os.OS);
        assertEquals("Ubuntu", os.prettyName);
    }

    @Test
    public void testUserAgentFirefoxOSX() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:12.0) Gecko/20100101 Firefox/12.0";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Firefox", browser.browser);
        assertEquals("12.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10.6", os.OS);
        assertEquals("OS X Snow Leopard", os.prettyName);
    }

    @Test
    public void testUserAgentFirefoxWinXP() {
        String userAgent = "Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20120405 Firefox/14.0a1";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Firefox", browser.browser);
        assertEquals("14.0a1", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 5.1", os.OS);
        assertEquals("Windows XP", os.prettyName);
    }

    @Test
    public void testUserAgentFirefoxLin() {
        String userAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1.16) Gecko/20120421 Gecko Firefox/11.0";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Firefox", browser.browser);
        assertEquals("11.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Linux i686", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentIceweaselLin() {
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:11.0a2) Gecko/20111230 Firefox/11.0a2 Iceweasel/11.0a2";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Iceweasel", browser.browser);
        assertEquals("11.0a2", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Linux x86_64", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentIE9Win7() {
        String userAgent = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; Media Center PC 6.0; InfoPath.3; MS-RTC LM 8; Zune 4.7)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("9.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 6.1", os.OS);
        assertEquals("Windows 7", os.prettyName);
    }

    @Test
    public void testUserAgentIE11Win81() {
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("11.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 6.3", os.OS);
        assertEquals("Windows 8.1", os.prettyName);
    }

    @Test
    public void testUserAgentIE11Win81WithDotNetVersion() {
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; .NET4.0E; .NET4.0C; rv:11.0) like Gecko";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("11.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 6.3", os.OS);
        assertEquals("Windows 8.1", os.prettyName);
    }

    @Test
    public void testUserAgentIE11Win81WithOtherVersionInfo() {
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; rv:11.0) like Gecko";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("11.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 6.3", os.OS);
        assertEquals("Windows 8.1", os.prettyName);
    }

    @Test
    public void testUserAgentIE8WinXP() {
        String userAgent = "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 1.1.4322)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("8.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 5.1", os.OS);
        assertEquals("Windows XP", os.prettyName);
    }

    @Test
    public void testUserAgentIE7Win95() {
        String userAgent = "Mozilla/4.0 (compatible; MSIE 7.0; Windows 95)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("MSIE", browser.browser);
        assertEquals("7.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows 95", os.OS);
        assertEquals("Windows 95", os.prettyName);
    }

    @Test
    public void testUserAgentSafariOSX() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/534.55.3 (KHTML, like Gecko) Version/5.1.3 Safari/534.53.10";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("5.1.3", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10_7_3", os.OS);
        assertEquals("OS X Lion", os.prettyName);

        userAgent = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; fr-fr) AppleWebKit/523.10.3 (KHTML, like Gecko) Version/3.0.4 Safari/523.10";
        browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("3.0.4", browser.version);
        os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentSafariIpod() {
        String userAgent = "Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_3 like Mac OS X; ja-jp) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("5.0.2", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("CPU iPhone OS 4_3_3 like Mac OS X", os.OS);
        assertEquals("iPod", os.prettyName);
    }

    @Test
    public void testUserAgentSafariIpad() {
        String userAgent = "Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko ) Version/5.1 Mobile/9B176 Safari/7534.48.3";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("5.1", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("CPU OS 5_1 like Mac OS X", os.OS);
        assertEquals("iPad", os.prettyName);
    }

    @Test
    public void testUserAgentSafariIphone() {
        String userAgent = "Mozilla/5.0 (iPhone; U; fr; CPU iPhone OS 4_2_1 like Mac OS X; fr) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148a Safari/6533.18.5";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("5.0.2", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("CPU iPhone OS 4_2_1 like Mac OS X", os.OS);
        assertEquals("iPhone", os.prettyName);
    }

    @Test
    public void testUserAgentSafariBlackBerry() {
        String userAgent = "Mozilla/5.0 (BlackBerry; U; BlackBerry 9900; en) AppleWebKit/534.11+ (KHTML, like Gecko) Version/7.1.0.346 Mobile Safari/534.11+";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("7.1.0.346", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("BlackBerry 9900", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentSafariAndroid() {
        String userAgent = "Mozilla/5.0 (Linux; U; Android 2.3.5; zh-cn; HTC_IncredibleS_S710e Build/GRJ90) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Safari", browser.browser);
        assertEquals("4.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("HTC_IncredibleS_S710e Build/GRJ90", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentRockmeltOSX() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.24 (KHTML, like Gecko) RockMelt/0.9.58.494 Chrome/11.0.696.71 Safari/534.24";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("RockMelt", browser.browser);
        assertEquals("0.9.58.494", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10_6_8", os.OS);
        assertEquals("OS X Snow Leopard", os.prettyName);
    }

    @Test
    public void testUserAgentOperaOSX() {
        String userAgent = "Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Opera", browser.browser);
        assertEquals("11.52", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10.6.8", os.OS);
        assertEquals("OS X Snow Leopard", os.prettyName);
    }

    @Test
    public void testUserAgentOperaSpoofingMozillaWinXP() {
        String userAgent = "Mozilla/5.0 (Windows NT 5.1; U; en; rv:1.8.1) Gecko/20061208 Firefox/5.0 Opera 11.11";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Firefox", browser.browser);
        assertEquals("5.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows NT 5.1", os.OS);
        assertEquals("Windows XP", os.prettyName);
    }

    @Test
    public void testUserAgentSeaMonkeyFreeBSD() {
        String userAgent = "Mozilla/5.0 (X11; FreeBSD amd64; rv:6.0) Gecko/20110818 Firefox/6.0 SeaMonkey/2.3";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("SeaMonkey", browser.browser);
        assertEquals("2.3", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("FreeBSD amd64", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentNintendoDS() {
        String userAgent = "Bunjalloo/0.7.6(Nintendo DS;U;en)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Bunjalloo", browser.browser);
        assertEquals("0.7.6", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Nintendo DS", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentPlaystation3() {
        String userAgent = "Mozilla/5.0 (PLAYSTATION 3; 3.55)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Mozilla", browser.browser);
        assertEquals("5.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("PLAYSTATION 3", os.OS);
        assertEquals("", os.prettyName);
    }

    @Test
    public void testUserAgentEpiphany() {
        String userAgent = "Mozilla/5.0 (X11; U; Linux x86_64; it-it) AppleWebKit/534.26+ (KHTML, like Gecko) Ubuntu/11.04 Epiphany/2.30.6)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("Epiphany", browser.browser);
        assertEquals("2.30.6", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Linux x86_64", os.OS);
        assertEquals("Ubuntu", os.prettyName);
    }

    @Test
    public void testUserAgentNokia() {
        String userAgent = "Mozilla/5.0 (SymbianOS/9.3; Series60/3.2 NokiaE52-1/052.003; Profile/MIDP-2.1 Configuration/CLDC-1.1 ) AppleWebKit/525 (KHTML, like Gecko) Version/3.0 BrowserNG/7.2.6.2 3gpp-gba";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("BrowserNG", browser.browser);
        assertEquals("3.0", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("SymbianOS/9.3", os.OS);
        assertEquals("Nokia", os.prettyName);
    }

    @Test
    public void testUserAgentITunes() {
        String userAgent = "iTunes/9.0.3 (Macintosh; U; Intel Mac OS X 10_6_2; en-ca)";
        SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("iTunes", browser.browser);
        assertEquals("9.0.3", browser.version);
        SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10_6_2", os.OS);
        assertEquals("OS X Snow Leopard", os.prettyName);

        userAgent = "iTunes/9.0.3";
        browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("iTunes", browser.browser);
        assertEquals("9.0.3", browser.version);
        os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("", os.OS);
        assertEquals("", os.prettyName);

        userAgent = "iTunes/9.0.2 (Windows; N)";
        browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("iTunes", browser.browser);
        assertEquals("9.0.2", browser.version);
        os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Windows", os.OS);
        assertEquals("", os.prettyName);

        userAgent = "itunes/9.0.2 (Macintosh; Intel Mac OS X 10.4.11) AppleWebKit/531.21.8";
        browser = UserAgentSniffer.sniffBrowser(userAgent);
        assertEquals("itunes", browser.browser);
        assertEquals("9.0.2", browser.version);
        os = UserAgentSniffer.sniffOS(userAgent);
        assertEquals("Intel Mac OS X 10.4.11", os.OS);
        assertEquals("OS X Tiger", os.prettyName);
    }
}
