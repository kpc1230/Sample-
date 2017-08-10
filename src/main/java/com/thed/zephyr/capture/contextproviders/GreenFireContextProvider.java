package com.thed.zephyr.capture.contextproviders;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.SessionCFTTransportObject;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.properties.BonfireConstants;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.TestingStatusService;
import com.thed.zephyr.capture.service.TestingStatusService.TestingStatus;
import com.thed.zephyr.capture.service.TestingStatusService.TestingStatusBar;
import com.thed.zephyr.capture.util.SessionDisplayUtils;
import com.thed.zephyr.capture.util.model.SessionDisplayHelper;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreenFireContextProvider implements ContextProvider {
    private static final String ISSUE_KEY = "issue";
    private static final String USER_KEY = "user";

    private static final String I18N = "i18n";
    private static final String TEXT_UTILS = "textutils";
    private static final String HAS_RELATED_TO = "hasRelatedTo";
    private static final String RELATED_TO = "relatedTo";
    private static final String TESTING_STATUS = "testingStatus";
    private static final String TESTING_BAR = "testingBar";
    private static final String GH_MAGIC_COUNT_PARAM = "atl.gh.issue.details.tab.count";
    private static final String XSRF_TOKEN = "xsrfToken";
    private static final String BASE_URL = "baseurl";

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = TestingStatusService.SERVICE)
    private TestingStatusService testingStatusService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = SessionDisplayUtils.SERVICE)
    private SessionDisplayUtils displayUtils;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @JIRAResource
    private VelocityRequestContextFactory velocityRequestContextFactory;

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context) {
        final Object user = context.get(USER_KEY);
        ApplicationUser appUser = user instanceof ApplicationUser ? (ApplicationUser) user : ApplicationUsers.from((User) user);
        Issue issue = (Issue) context.get(ISSUE_KEY);
        Map<String, Object> params = new HashMap<String, Object>(context);
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        List<SessionCFTTransportObject> relatedTo = getRelatedToSessions(appUser, relatedToField.getValueFromIssue(issue));
        TestingStatus testingStatus = testingStatusService.getTestingStatus(issue);
        TestingStatusBar testingBar = testingStatusService.getTestingStatusBar(issue);

        params.put(BASE_URL, velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        params.put(HAS_RELATED_TO, !relatedTo.isEmpty());
        params.put(RELATED_TO, relatedTo);
        params.put(TESTING_STATUS, i18n.getText(testingStatus.getI18nKey()));
        params.put(TESTING_BAR, testingBar);
        params.put(GH_MAGIC_COUNT_PARAM, Long.valueOf(relatedTo.size()));
        params.put(XSRF_TOKEN, new JiraWebActionSupport().getXsrfToken());
        params.put(I18N, i18n);

        params.put(TEXT_UTILS, new TextUtils());

        return params;
    }

    // TODO merge common methods with BonfireCFVelocityContext
    private List<SessionCFTTransportObject> getRelatedToSessions(ApplicationUser user, String rawSessionIds) {
        List<SessionCFTTransportObject> toReturn = Lists.newArrayList();
        if (!StringUtils.isEmpty(rawSessionIds)) {
            String[] split = rawSessionIds.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            for (String s : split) {
                LightSession session = sessionController.getLightSession(s);
                if (session != null) {
                    ApplicationUser assignee = session.getAssignee();
                    SessionDisplayHelper flags = displayUtils.getDisplayHelper(user, session);
                    toReturn.add(new SessionCFTTransportObject(session.getId().toString(), BonfireConstants.SESSION_PAGE + s, session.getName(),
                            getUserAvatarUrl(assignee), assignee.getName(), getPrettyStatus(session.getStatus()), flags));
                }
            }
        }
        return toReturn;
    }

    private String getUserAvatarUrl(ApplicationUser user) {
        return excaliburWebUtil.getSmallAvatarUrl(user);
    }

    private String getPrettyStatus(Status status) {
        return i18n.getText("session.status.pretty." + status);
    }
}
