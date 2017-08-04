package com.atlassian.excalibur.index;

import com.atlassian.bonfire.model.IndexedTemplate;
import com.atlassian.excalibur.index.iterators.IndexUtils;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests IndexUtils operations
 *
 * @since v1.7
 */
public class TestIndexUtils {
    @Test
    public void testDoubleDelete() throws Exception {
        assertEquals(IndexUtils.deleteFromIndex(1L, jsonArrayIterator123()), "[3,2]");

        assertEquals(IndexUtils.deleteFromIndex(1L, new JSONArrayIterator(
                IndexUtils.deleteFromIndex(1L, jsonArrayIterator123()))), "[3,2]");

        assertEquals(IndexUtils.deleteFromIndex(2L, new JSONArrayIterator(
                IndexUtils.deleteFromIndex(2L, jsonArrayIterator123()))), "[3,1]");

        assertEquals(IndexUtils.deleteFromIndex(3L, new JSONArrayIterator(
                IndexUtils.deleteFromIndex(3L, jsonArrayIterator123()))), "[2,1]");
    }

    @Test
    public void testDoubleAdd() throws Exception {
        assertEquals(IndexUtils.addToIndex(1L, jsonArrayIterator123()), "[3,2,1]");
        assertEquals(IndexUtils.addToIndex(2L, jsonArrayIterator123()), "[3,2,1]");
        assertEquals(IndexUtils.addToIndex(3L, jsonArrayIterator123()), "[3,2,1]");
    }

    @Test
    public void testDoubleDeleteTemplate() throws Exception {
        assertEquals(IndexUtils.deleteFromJSONIndex(1L, jsonArrayIteratorTemplates()), "[{\"id\":3,\"projectId\":1},{\"id\":2,\"projectId\":1}]");

        assertEquals(IndexUtils.deleteFromJSONIndex(1L, new JSONArrayIterator(
                IndexUtils.deleteFromJSONIndex(1L, jsonArrayIteratorTemplates()))), "[{\"id\":3,\"projectId\":1},{\"id\":2,\"projectId\":1}]");

        assertEquals(IndexUtils.deleteFromJSONIndex(2L, new JSONArrayIterator(
                IndexUtils.deleteFromJSONIndex(2L, jsonArrayIteratorTemplates()))), "[{\"id\":3,\"projectId\":1},{\"id\":1,\"projectId\":1}]");

        assertEquals(IndexUtils.deleteFromJSONIndex(3L, new JSONArrayIterator(
                IndexUtils.deleteFromJSONIndex(3L, jsonArrayIteratorTemplates()))), "[{\"id\":2,\"projectId\":1},{\"id\":1,\"projectId\":1}]");
    }

    @Test
    public void testDoubleAddTemplate() throws Exception {
        assertEquals(IndexUtils.addToJSONIndex(new IndexedTemplate(1L, 1L).toJSON(), jsonArrayIteratorTemplates()), "[{\"id\":3,\"projectId\":1},{\"id\":2,\"projectId\":1},{\"id\":1,\"projectId\":1}]");
        assertEquals(IndexUtils.addToJSONIndex(new IndexedTemplate(2L, 1L).toJSON(), jsonArrayIteratorTemplates()), "[{\"id\":3,\"projectId\":1},{\"id\":2,\"projectId\":1},{\"id\":1,\"projectId\":1}]");
        assertEquals(IndexUtils.addToJSONIndex(new IndexedTemplate(3L, 1L).toJSON(), jsonArrayIteratorTemplates()), "[{\"id\":3,\"projectId\":1},{\"id\":2,\"projectId\":1},{\"id\":1,\"projectId\":1}]");
    }

    private JSONArrayIterator jsonArrayIterator123() {
        return new JSONArrayIterator(IndexUtils.addToIndex(3L,
                new JSONArrayIterator(IndexUtils.addToIndex(2L,
                        new JSONArrayIterator(IndexUtils.addToIndex(1L,
                                new JSONArrayIterator("[]")))))));
    }

    private JSONArrayIterator jsonArrayIteratorTemplates() {
        return new JSONArrayIterator(IndexUtils.addToJSONIndex(new IndexedTemplate(1L, 1L).toJSON(),
                new JSONArrayIterator(IndexUtils.addToJSONIndex(new IndexedTemplate(2L, 1L).toJSON(),
                        new JSONArrayIterator(IndexUtils.addToJSONIndex(new IndexedTemplate(3L, 1L).toJSON(),
                                new JSONArrayIterator("[]")))))));
    }
}
