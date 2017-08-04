package com.atlassian.excalibur.view;

import com.atlassian.excalibur.model.Tag;
import com.opensymphony.util.TextUtils;

/**
 * @since v1.4
 */
public class TagUI {
    private final FatNoteUI fatNoteUI;
    private final Tag tag;

    public TagUI(final FatNoteUI fatNoteUI, final Tag tag) {
        this.fatNoteUI = fatNoteUI;
        this.tag = tag;
    }

    public String getName() {
        return TextUtils.htmlEncode(tag.getName());
    }

    public Long getId() {
        return tag.getId();
    }
}
