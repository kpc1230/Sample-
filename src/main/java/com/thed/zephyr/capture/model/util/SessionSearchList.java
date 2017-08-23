package com.thed.zephyr.capture.model.util;

import com.thed.zephyr.capture.model.Session;

import java.util.List;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
public class SessionSearchList extends SearchList<Session> {

    public SessionSearchList(List<Session> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}
