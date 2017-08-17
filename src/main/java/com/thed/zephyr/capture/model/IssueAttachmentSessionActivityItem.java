package com.thed.zephyr.capture.model;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.model.jira.Issue;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueAttachmentSessionActivityItem extends BaseSessionActivityItem {
    public static final String templateLocation = "/templates/bonfire/web/stream/issue-attachment.vm";

    private final Long issueId;

    private final Issue issue;

    private final Attachment attachment;

    private final Long attachmentId;

    /**
     * Use the ThumbnailManager to get this. The thumbnail manager will check all the permissions and create a thumbnail if there isn't one. This will
     * be null if there is no thumbnail.
     */
    private final Thumbnail thumbnail;

    public IssueAttachmentSessionActivityItem(DateTime timestamp, String user, Long issueId, Issue issue, Long attachmentId, Attachment attachment,
                                              Thumbnail thumbnail) {
        super(timestamp, user, CaptureUtil.getLargeAvatarUrl(user));
        this.issueId = issueId;
        this.issue = issue;
        this.attachment = attachment;
        this.attachmentId = attachmentId;
        this.thumbnail = thumbnail;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Issue getIssue() {
        return issue;
    }

    public String getSummary() {
        return issue.getSummary();
    }

    public Issue getParentIssue() {
        if (issue != null) {
            return issue.getParentObject();
        }
        return null;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public String getAttachmentFilename() {
        return attachment == null ? "" : attachment.getFileName();
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    @Override
    public String getTemplateName() {
        return templateLocation;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
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

        IssueAttachmentSessionActivityItem that = (IssueAttachmentSessionActivityItem) o;

        if (attachment != null ? !attachment.equals(that.attachment) : that.attachment != null) {
            return false;
        }
        if (attachmentId != null ? !attachmentId.equals(that.attachmentId) : that.attachmentId != null) {
            return false;
        }
        if (issue != null ? !issue.equals(that.issue) : that.issue != null) {
            return false;
        }
        if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null) {
            return false;
        }
        if (thumbnail != null ? !thumbnail.equals(that.thumbnail) : that.thumbnail != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (attachment != null ? attachment.hashCode() : 0);
        result = 31 * result + (attachmentId != null ? attachmentId.hashCode() : 0);
        result = 31 * result + (thumbnail != null ? thumbnail.hashCode() : 0);
        return result;
    }
}
