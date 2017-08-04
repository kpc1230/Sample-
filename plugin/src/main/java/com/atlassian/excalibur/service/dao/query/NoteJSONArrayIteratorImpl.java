package com.atlassian.excalibur.service.dao.query;

import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.json.JSONException;


/**
 * Wraps a JSONArrayIterator and turns it into an Long Iterator
 *
 * @since v1.4
 */
public class NoteJSONArrayIteratorImpl implements NoteJSONArrayIterator {
    private JSONArrayIterator jsonArrayIterator;

    public NoteJSONArrayIteratorImpl(JSONArrayIterator jsonArrayIterator) {
        if (jsonArrayIterator == null) {
            try {
                this.jsonArrayIterator = new JSONArrayIterator("[]");
            } catch (JSONException e) {
                throw new RuntimeException("Error creating empty note json array iterator", e);
            }
        } else {
            this.jsonArrayIterator = jsonArrayIterator;
        }
    }

    public boolean hasNext() {
        return jsonArrayIterator.hasNext();
    }

    public Long next() {
        return convertJSONArrayIteratorObject(jsonArrayIterator.next());
    }

    public void remove() {
        jsonArrayIterator.remove();
    }

    public void skip() {
        jsonArrayIterator.skip();
    }

    private Long convertJSONArrayIteratorObject(Object object) {
        // TODO Make jsonArrayIterator always return Long instead of Integer
        try {
            return ((Integer) object).longValue();
        } catch (ClassCastException e) {
            return (Long) object;
        }
    }

}