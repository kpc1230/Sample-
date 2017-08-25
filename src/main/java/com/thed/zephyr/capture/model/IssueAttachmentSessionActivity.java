package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.service.db.AttachmentTypeConverter;
import com.thed.zephyr.capture.service.db.IssueTypeConverter;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueAttachmentSessionActivity extends SessionActivity {

    @DynamoDBTypeConverted(converter = IssueTypeConverter.class)
    private Issue issue;

    @DynamoDBTypeConverted(converter = AttachmentTypeConverter.class)
    private Attachment attachment;

    public IssueAttachmentSessionActivity() {
    }

    public IssueAttachmentSessionActivity(String sessionId, String clientKey, DateTime timestamp, String user, Issue issue, Attachment attachment) {
        super(sessionId, clientKey, timestamp, user, CaptureUtil.getLargeAvatarUrl(user));
        this.issue = issue;
        this.attachment = attachment;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IssueAttachmentSessionActivity that = (IssueAttachmentSessionActivity) o;

        if (attachment != null ? !attachment.equals(that.attachment) : that.attachment != null) {
            return false;
        }if (issue != null ? !issue.equals(that.issue) : that.issue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (attachment != null ? attachment.hashCode() : 0);
        return result;
    }
}
