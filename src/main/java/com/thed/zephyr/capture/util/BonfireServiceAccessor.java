package com.thed.zephyr.capture.util;

import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.TestingStatusService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * This is used to access Bonfire Services where we can't normally access them. Only use when absolutely necessary. In general if you require this
 * then you are probably doing something wrong... unless it is something JIRA framework related, then it is a necessary evil.
 *
 * @author ezhang
 */
@Service
public class BonfireServiceAccessor implements InitializingBean {
    private static BonfireServiceAccessor instance;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = TestingStatusService.SERVICE)
    private TestingStatusService testingStatusService;

    public static BonfireServiceAccessor getInstance() {
        return instance;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public BonfireI18nService getI18n() {
        return i18n;
    }

    public TestingStatusService getTestingStatusService() {
        return testingStatusService;
    }
}
