package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.service.db.AttachmentTypeConverter;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueAttachmentSessionActivity extends SessionActivity {

    private Long issueId;

    @DynamoDBTypeConverted(converter = AttachmentTypeConverter.class)
    private Attachment attachment;

    public IssueAttachmentSessionActivity() {
    }

    public IssueAttachmentSessionActivity(Long issueId, Attachment attachment) {
        this.issueId = issueId;
        this.attachment = attachment;
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
        }if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        result = 31 * result + (attachment != null ? attachment.hashCode() : 0);
        return result;
    }
}
