package com.atlassian.excalibur.service.dao.query;


import org.apache.commons.lang.NotImplementedException;

import java.util.Iterator;

/**
 * Performing an OR operation across two Note id iterators
 * Preserves the ordering of the note id iterators (descending)
 * Doesn't return duplicates
 *
 * @since v1.4
 */
public class NoteJSONArrayIteratorOR implements NoteJSONArrayIterator {
    private final Iterator<Long> firstJsonArrayIterator;
    private final Iterator<Long> secondJsonArrayIterator;
    private Long currentFirstNoteId;
    private Long currentSecondNoteId;

    public NoteJSONArrayIteratorOR(Iterator<Long> firstJsonArrayIterator, Iterator<Long> secondJsonArrayIterator) {
        this.firstJsonArrayIterator = firstJsonArrayIterator;
        this.secondJsonArrayIterator = secondJsonArrayIterator;

        advanceFirstNoteId();
        advanceSecondNoteId();
    }


    public boolean hasNext() {
        return !currentFirstNoteId.equals(-1L) || !currentSecondNoteId.equals(-1L);
    }

    public Long next() {
        if (currentFirstNoteId > currentSecondNoteId) {
            Long noteId = currentFirstNoteId;

            advanceFirstNoteId();

            return noteId;
        }

        if (currentSecondNoteId > currentFirstNoteId) {
            Long noteId = currentSecondNoteId;

            advanceSecondNoteId();

            return noteId;
        }

        if (currentFirstNoteId.equals(currentSecondNoteId)) {
            Long noteId = currentFirstNoteId;

            advanceFirstNoteId();
            advanceSecondNoteId();

            return noteId;
        }

        // Should we throw an exception in this case?
        return null;
    }

    private void advanceSecondNoteId() {
        if (secondJsonArrayIterator.hasNext()) {
            currentSecondNoteId = secondJsonArrayIterator.next();
        } else {
            currentSecondNoteId = -1L;
        }
    }

    private void advanceFirstNoteId() {
        if (firstJsonArrayIterator.hasNext()) {
            currentFirstNoteId = firstJsonArrayIterator.next();
        } else {
            currentFirstNoteId = -1L;
        }
    }

    public void remove() {
        throw new NotImplementedException();
    }

    public void skip() {
        next();
    }
}
