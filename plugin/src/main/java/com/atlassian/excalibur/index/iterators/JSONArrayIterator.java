package com.atlassian.excalibur.index.iterators;

import com.atlassian.json.JSONException;
import com.atlassian.json.JSONTokener;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for a JSONArray.
 * Each .next() call yields the next element in the JSONArray
 *
 * @since v1.3
 */
public class JSONArrayIterator implements Iterator<Object> {
    final private JSONTokener jsonTokener;

    public JSONArrayIterator(String sourceData) throws JSONException {
        if (StringUtils.isEmpty(sourceData)) {
            sourceData = "[]";
        }
        this.jsonTokener = new JSONTokener(sourceData);
        if (jsonTokener.nextClean() != '[') {
            jsonTokener.syntaxError("A JSONArray text must start with '['");
        }
    }

    public boolean hasNext() {
        boolean hasNext = false;
        try {
            char nextToken = jsonTokener.nextClean();
            hasNext = (nextToken != ']' && nextToken != '\u0000');
        } catch (JSONException e) {
            return false;
        }
        jsonTokener.back();
        return hasNext;
    }

    public Object next() {
        Object returnObject = null;
        try {
            if (',' == jsonTokener.nextClean()) {
                jsonTokener.back();
                returnObject = null;
            } else {
                jsonTokener.back();
                returnObject = jsonTokener.nextValue();
            }
            switch (jsonTokener.nextClean()) {
                case ';':
                    break;
                case ',':
                    break;
                case ']':
                    break;
                default:
                    jsonTokener.syntaxError("Expected a ',' or ']'");
            }
        } catch (JSONException e) {
            throw new NoSuchElementException();
        }
        return returnObject;
    }

    public void skip() {
        // TODO Make this less naive - this will only work for primitive objects in the iterator
        if (',' == jsonTokener.skipTo(',')) {
            jsonTokener.next();
        }
    }

    public void remove() {
        throw new NotImplementedException();
    }

}
