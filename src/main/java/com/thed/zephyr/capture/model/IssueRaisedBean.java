package com.thed.zephyr.capture.model;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author manjunath
 *
 */
public class IssueRaisedBean implements Comparable<IssueRaisedBean> {

	private Long issueId;

	private Date timeCreated;
	
	public IssueRaisedBean() {
	}
	
	public IssueRaisedBean(Long issueId, Date timeCreated) {
		this.issueId = issueId;
		this.timeCreated = timeCreated;
	}

	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((issueId == null) ? 0 : issueId.hashCode());
		result = prime * result + ((timeCreated == null) ? 0 : timeCreated.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssueRaisedBean other = (IssueRaisedBean) obj;
		if (issueId == null) {
			if (other.issueId != null)
				return false;
		} else if (!issueId.equals(other.issueId))
			return false;
		if (timeCreated == null) {
			if (other.timeCreated != null)
				return false;
		} else if (!timeCreated.equals(other.timeCreated))
			return false;
		return true;
	}

	@Override
	public int compareTo(IssueRaisedBean that) {
		if (Objects.isNull(that)) {
            return -1;
        }
		int result = 0;
		if(Objects.nonNull(this.issueId) && Objects.nonNull(that.issueId)) {
			result = this.issueId.compareTo(that.issueId);
		} else if(Objects.nonNull(this.issueId) && Objects.isNull(that.issueId)) {
			result = 1;
		} else {
			result = -1;
		}
		if(result == 0) {
			if(Objects.nonNull(this.timeCreated) && Objects.nonNull(that.timeCreated)) {
				result = this.timeCreated.compareTo(that.timeCreated);
			} else if(Objects.nonNull(this.timeCreated) && Objects.isNull(that.timeCreated)) {
				result = 1;
			} else {
				result = -1;
			}
		}
		return result;
	}
	
	public String toJSON() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);
        return jsonNode.toString();
    }
	
}
