package com.thed.zephyr.capture.model.util;

import com.thed.zephyr.capture.model.LightSession;

import java.util.List;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
public class LightSessionSearchList extends SearchList<LightSession> {
    public LightSessionSearchList(List<LightSession> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
