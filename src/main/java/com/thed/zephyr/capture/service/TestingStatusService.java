package com.thed.zephyr.capture.service;

import com.atlassian.jira.issue.Issue;

public interface TestingStatusService {
    public static final String SERVICE = "bonfire-TestingStatusService";

    public enum TestingStatus {
        NOT_STARTED("bonfire.testingstatus.notstarted"),
        IN_PROGRESS("bonfire.testingstatus.inprogress"),
        INCOMPLETE("bonfire.textingstatus.incomplete"),
        COMPLETED("bonfire.textingstatus.complete");

        private final String i18nKey;

        private TestingStatus(final String i18nKey) {
            this.i18nKey = i18nKey;
        }

        public String getI18nKey() {
            return i18nKey;
        }

        public static TestingStatus fromi18nKey(String i18nKey) {
            for (TestingStatus status : TestingStatus.values()) {
                if (status.i18nKey.equalsIgnoreCase(i18nKey)) {
                    return status;
                }
            }
            return null;
        }
    }

    /**
     * Loads all related test sessions and Calculates TestingStatus value for issue
     */
    public TestingStatus calculateTestingStatus(Issue issue);

    /**
     * Reads value of TestingStatus from DB
     */
    public TestingStatus getTestingStatus(Issue issue);

    public TestingStatusBar getTestingStatusBar(Issue issue);

    public static class TestingStatusBar {
        private final int notstartedPercent;
        private final int inprogressPercent;
        private final int completePercent;
        private final int completeCount;
        private final int totalCount;

        public TestingStatusBar(int notstartedPercent, int inprogressPercent, int completePercent, int completeCount, int totalCount) {
            this.notstartedPercent = notstartedPercent;
            this.inprogressPercent = inprogressPercent;
            this.completePercent = completePercent;
            this.completeCount = completeCount;
            this.totalCount = totalCount;
        }

        public String getNotstartedPercent() {
            return String.valueOf(notstartedPercent);
        }

        public String getInprogressPercent() {
            return String.valueOf(inprogressPercent);
        }

        public String getCompletePercent() {
            return String.valueOf(completePercent);
        }

        public String getCompleteCount() {
            return String.valueOf(completeCount);
        }

        public String getTotalCount() {
            return String.valueOf(totalCount);
        }
    }
}
