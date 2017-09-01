package com.thed.zephyr.capture.model.view;

import com.opensymphony.util.TextUtils;
import com.thed.zephyr.capture.model.Tag;


/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class TagUI {
    private final FatNoteUI fatNoteUI;
    private final String tag;

    public TagUI(final FatNoteUI fatNoteUI, final String tag) {
        this.fatNoteUI = fatNoteUI;
        this.tag = tag;
    }

    public String getName() {
        return TextUtils.htmlEncode(tag);
    }

    public String getTag() {
        return tag;
    }
}
