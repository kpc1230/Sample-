package com.thed.zephyr.capture.model;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.joda.time.DateTime;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class IssueAttachmentSessionActivity extends SessionActivity {

    private final Issue issue;

    private final Attachment attachment;

    /**
     * Use the ThumbnailManager to get this. The thumbnail manager will check all the permissions and create a thumbnail if there isn't one. This will
     * be null if there is no thumbnail.
     */
    private final Thumbnail thumbnail;

    public IssueAttachmentSessionActivity(String sessionId, DateTime timestamp, String user, Issue issue, Attachment attachment,
                                          Thumbnail thumbnail) {
        super(sessionId, timestamp, user, CaptureUtil.getLargeAvatarUrl(user));
        this.issue = issue;
        this.attachment = attachment;
        this.thumbnail = thumbnail;
    }

    public Issue getIssue() {
        return issue;
    }

    public Attachment getAttachment() {
        return attachment;
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

        IssueAttachmentSessionActivity that = (IssueAttachmentSessionActivity) o;

        if (attachment != null ? !attachment.equals(that.attachment) : that.attachment != null) {
            return false;
        }if (issue != null ? !issue.equals(that.issue) : that.issue != null) {
            return false;
        }if (thumbnail != null ? !thumbnail.equals(that.thumbnail) : that.thumbnail != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (attachment != null ? attachment.hashCode() : 0);
        result = 31 * result + (thumbnail != null ? thumbnail.hashCode() : 0);
        return result;
    }
}
