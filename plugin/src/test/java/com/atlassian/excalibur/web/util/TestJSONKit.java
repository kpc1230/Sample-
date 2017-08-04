package com.atlassian.excalibur.web.util;

import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TestJSONKit {

    public static final String NEW_PROPERTY = "newProperty";
    private JSONObject json;
    private JSONObject json2;
    private JSONObject emptyJSON;
    private JSONArray emptyArray;
    private JSONObject parent;

    private JSONArray parentArr;

    @Before
    public void setUp() throws Exception {
        emptyJSON = new JSONObject();
        emptyArray = new JSONArray();

        json = new JSONObject();
        json.put("propertyOne", "valueOne");

        json2 = new JSONObject();
        json2.put("propertyTwo", "valueTwo");

        parent = new JSONObject();

        parentArr = new JSONArray();
        parentArr.put(json);
        parentArr.put(json2);

    }

    @Test
    public void testPutAndGet() throws Exception {
        JSONObject result = JSONKit.put(parent, "newProperty", json);
        assertSame(parent, result);
        assertEquals(parent.get("newProperty"), json);

        result = JSONKit.get(parent, "null");
        assertEquals(emptyJSON, result);

        result = JSONKit.get(parent, "null", json);
        assertEquals(json, result);

        result = JSONKit.get(parent, NEW_PROPERTY);
        assertEquals(json, result);
    }


    @Test
    public void testToJSON() throws Exception {

        JSONObject result = JSONKit.to("bad");
        assertEquals(emptyJSON, result);

        result = JSONKit.to("bad", json2);
        assertEquals(json2, result);

        result = JSONKit.to("{}");
        assertEquals(emptyJSON, result);

        result = JSONKit.to(json2.toString());
        assertEquals(json2, result);

        JSONArray resultArray = JSONKit.toArray("bad");
        assertEquals(emptyArray, resultArray);

        resultArray = JSONKit.toArray(null);
        assertEquals(emptyArray, resultArray);

        resultArray = JSONKit.toArray("bad", parentArr);
        assertEquals(parentArr, resultArray);

        resultArray = JSONKit.toArray("{}");
        assertEquals(emptyArray, resultArray);

        resultArray = JSONKit.toArray("[]");
        assertEquals(emptyArray, resultArray);

        resultArray = JSONKit.toArray(parentArr.toString());
        assertEquals(parentArr, resultArray);

    }
}
