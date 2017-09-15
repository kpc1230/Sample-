package com.thed.zephyr.capture.model.jira;

/**
 * Created by niravshah on 9/7/17.
 */

public class TestingStatus  {
    private Double notStartedPercent;
    private Double inProgressPercent;
    private Double completePercent;
    private Double completeCount;
    private Double totalCount;
    private Double totalSessions;
    private String testingStatusEnum;

    public TestingStatus(Double notStartedPercent, Double inProgressPercent, Double completePercent, Double completeCount, Double totalCount) {
        this.notStartedPercent = notStartedPercent;
        this.inProgressPercent = inProgressPercent;
        this.completePercent = completePercent;
        this.completeCount = completeCount;
        this.totalCount = totalCount;
    }

    public TestingStatus() {

    }

    public Double getNotStartedPercent() {
        return notStartedPercent;
    }

    public void setNotStartedPercent(Double notStartedPercent) {
        this.notStartedPercent = notStartedPercent;
    }

    public Double getCompletePercent() {
        return completePercent;
    }

    public void setCompletePercent(Double completePercent) {
        this.completePercent = completePercent;
    }

    public Double getCompleteCount() {
        return completeCount;
    }

    public void setCompleteCount(Double completeCount) {
        this.completeCount = completeCount;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public Double getInProgressPercent() {
        return inProgressPercent;
    }

    public void setInProgressPercent(Double inProgressPercent) {
        this.inProgressPercent = inProgressPercent;
    }

    public String getTestingStatusEnum() {
        return testingStatusEnum;
    }

    public void setTestingStatusEnum(String testingStatusEnum) {
        this.testingStatusEnum = testingStatusEnum;
    }

    public Double getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Double totalSessions) {
        this.totalSessions = totalSessions;
    }

    public enum TestingStatusEnum {
        NOT_STARTED("bonfire.testingstatus.notstarted"),
        IN_PROGRESS("bonfire.testingstatus.inprogress"),
        INCOMPLETE("bonfire.textingstatus.incomplete"),
        COMPLETED("bonfire.textingstatus.complete");

        private final String i18nKey;

        private TestingStatusEnum(final String i18nKey) {
            this.i18nKey = i18nKey;
        }

        public String getI18nKey() {
            return i18nKey;
        }
    }
}