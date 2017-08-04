package com.atlassian.excalibur.service.dao.query;

import java.util.Iterator;

/**
 * NoteJSONArrayIterators need to have a skip() method
 *
 * @since v1.4
 */
public interface NoteJSONArrayIterator extends Iterator<Long> {
    public void skip();
}
