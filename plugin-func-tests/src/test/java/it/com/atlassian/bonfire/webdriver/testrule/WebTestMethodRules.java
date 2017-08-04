package it.com.atlassian.bonfire.webdriver.testrule;

import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.testing.rule.WebDriverScreenshotRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class WebTestMethodRules
{
    public static TestRule createRules(AtlassianWebDriver driver) {
        return RuleChain.outerRule(new WebDriverScreenshotRule(driver))
                .around(new ConsoleOutputRule());
    }
}