package com.atlassian.excalibur.service.dao.query;

import org.apache.commons.lang.NotImplementedException;

import java.util.Iterator;

/**
 * Performs an AND operation across two Note id iterators
 *
 * @since v1.4
 */
public class NoteJSONArrayIteratorAND implements NoteJSONArrayIterator {
    private final Iterator<Long> firstJsonArrayIterator;
    private final Iterator<Long> secondJsonArrayIterator;
    private Long currentFirstNoteId;
    private Long currentSecondNoteId;
    private Long nextId;
    private boolean hasNext = true;

    public NoteJSONArrayIteratorAND(NoteJSONArrayIterator firstJsonArrayIterator, NoteJSONArrayIterator secondJsonArrayIterator) {
        this.firstJsonArrayIterator = firstJsonArrayIterator;
        this.secondJsonArrayIterator = secondJsonArrayIterator;

        if (!(firstJsonArrayIterator.hasNext() && secondJsonArrayIterator.hasNext())) {
            // Need at least 1 element in each in order to be able to perform an AND operation
            hasNext = false;
        } else {
            this.currentFirstNoteId = firstJsonArrayIterator.next();
            this.currentSecondNoteId = secondJsonArrayIterator.next();
            preloadNext();
        }
    }

    /**
     * <p>
     * Preloads the next id to return.
     * </p>
     * <p>
     * This is the easiest way to be able to answer "hasNext()" because hasNext() requires index scanning anyway
     * </p>
     */
    private void preloadNext() {
        while (firstJsonArrayIterator.hasNext() && secondJsonArrayIterator.hasNext()) {
            if (currentSecondNoteId > currentFirstNoteId) {
                currentSecondNoteId = secondJsonArrayIterator.next();
                continue;
            }
            if (currentFirstNoteId > currentSecondNoteId) {
                currentFirstNoteId = firstJsonArrayIterator.next();
                continue;
            }
            // Found a match
            nextId = currentFirstNoteId;
            // Need to advance one of them - which one is more likely to run out?
            currentFirstNoteId = firstJsonArrayIterator.next();
            return;
        }

        // Could run out of one, but not the other
        while (firstJsonArrayIterator.hasNext() && currentFirstNoteId > currentSecondNoteId) {
            if (!currentFirstNoteId.equals(currentSecondNoteId)) {
                currentFirstNoteId = firstJsonArrayIterator.next();
                continue;
            }

            // Found a match
            nextId = currentFirstNoteId;
            currentFirstNoteId = firstJsonArrayIterator.next();
            return;
        }

        while (secondJsonArrayIterator.hasNext() && currentSecondNoteId > currentFirstNoteId) {
            if (!currentSecondNoteId.equals(currentFirstNoteId)) {
                currentSecondNoteId = secondJsonArrayIterator.next();
                continue;
            }

            // Found a match
            nextId = currentSecondNoteId;
            currentSecondNoteId = secondJsonArrayIterator.next();
            return;
        }

        // Could have a match on the last element
        if (currentFirstNoteId.equals(currentSecondNoteId)) {
            nextId = currentSecondNoteId;
            // Need to change one of the IDs to an invalid id so this doesn't keep evaluating to true
            currentFirstNoteId = -1L;
            return;
        }

        // No more matches
        hasNext = false;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public Long next() {
        // Work out the next to return
        Long nextToReturn = nextId;
        // Preload the next one after that (as it's the easiest way to be able to answer "hasNext()"
        preloadNext();
        return nextToReturn;
    }

    public void remove() {
        throw new NotImplementedException();
    }

    public void skip() {
        // Can't do a true skip as we'd lose our place. But we can call next() and throw away the result.
        next();
    }
}
