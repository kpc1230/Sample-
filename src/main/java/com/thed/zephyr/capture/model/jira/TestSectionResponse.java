package com.thed.zephyr.capture.model.jira;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thed.zephyr.capture.model.view.SessionDto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by niravshah on 9/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestSectionResponse implements Serializable {

    /**
	 * Generated Serial Version id.
	 */
	private static final long serialVersionUID = -6863061394674732559L;

	public TestSectionResponse(){}

    @JsonProperty
    private List<SessionDto> sessions;

    @JsonProperty
    private CaptureEnvironment captureEnvironment;
    
    private SessionDto raisedDuringSessionDto;

    @JsonProperty
    private BasicIssue raisedIssue;

    @JsonProperty
    private TestingStatus testingStatus;

    public TestingStatus getTestingStatus() {
        return testingStatus;
    }

    public void setTestingStatus(TestingStatus testingStatus) {
        this.testingStatus = testingStatus;
    }

    public List<SessionDto> getSessions() {
        return sessions;
    }

    public void setSessions(List<SessionDto> sessions) {
        this.sessions = sessions;
    }

    public CaptureEnvironment getCaptureEnvironment() {
        return captureEnvironment;
    }

    public void setCaptureEnvironment(CaptureEnvironment captureEnvironment) {
        this.captureEnvironment = captureEnvironment;
    }

    public BasicIssue getRaisedIssue() {
        return raisedIssue;
    }

    public void setRaisedIssue(BasicIssue raisedIssue) {
        this.raisedIssue = raisedIssue;
    }

	public SessionDto getRaisedDuring() {
		return raisedDuringSessionDto;
	}

	public void setRaisedDuring(SessionDto raisedDuringSessionDto) {
		this.raisedDuringSessionDto = raisedDuringSessionDto;
	}
}