package com.thed.zephyr.capture.model.util;

import com.thed.zephyr.capture.model.view.SessionDto;

import java.util.List;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
public class SessionDtoSearchList extends SearchList<SessionDto> {
    public SessionDtoSearchList(List<SessionDto> content, int offset, int limit, long total) {
        super(content, offset, limit, total);
    }
}