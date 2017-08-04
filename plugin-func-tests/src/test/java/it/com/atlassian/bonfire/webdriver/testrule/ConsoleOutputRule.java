package it.com.atlassian.bonfire.webdriver.testrule;

import com.atlassian.webdriver.testing.rule.JavaScriptErrorsRule;
import com.google.common.collect.Sets;
import org.junit.rules.TestName;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ConsoleOutputRule extends TestName
{
    private final Set<String> errorsToIgnore = Sets.newHashSet();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JavaScriptErrorsRule output = new JavaScriptErrorsRule();

    @Override
    protected void finished(Description description) {
        output.finished(description);
    }
}