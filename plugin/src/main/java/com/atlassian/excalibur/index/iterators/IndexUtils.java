package com.atlassian.excalibur.index.iterators;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONObject;

/**
 * Extracts out addition and deletion from indexes
 *
 * @since v1.6
 */
public class IndexUtils {
    public static String deleteFromIndex(Long id, JSONArrayIterator jsonArrayIterator) {
        StringBuilder rebuiltIndex = new StringBuilder("[");

        while (jsonArrayIterator.hasNext()) {
            String idString = String.valueOf(jsonArrayIterator.next());
            Long currentId = Long.parseLong(idString);

            if (currentId.equals(id)) {
                // Delete
                continue;
            }

            // Avoid doubling in size
            rebuiltIndex.ensureCapacity(idString.length() + 1);

            rebuiltIndex.append(idString);
            rebuiltIndex.append(',');
        }
        rebuiltIndex.setCharAt(rebuiltIndex.length() - 1, ']');
        if (rebuiltIndex.length() <= 1) {
            return "[]";
        } else {
            return rebuiltIndex.toString();
        }
    }

    public static String addToIndex(Long id, JSONArrayIterator jsonArrayIterator) {
        StringBuilder rebuiltIndex = new StringBuilder("[");

        if (!jsonArrayIterator.hasNext()) {
            rebuiltIndex.append(id.toString());
            rebuiltIndex.append("]");
            return rebuiltIndex.toString();
        }

        boolean inserted = false;

        while (jsonArrayIterator.hasNext()) {
            String idString = String.valueOf(jsonArrayIterator.next());
            Long currentId = Long.parseLong(idString);

            if (!inserted && currentId.equals(id)) {
                // If it's already in this index, don't worry about it
                inserted = true;
            }
            if (!inserted && currentId < id) {
                // Insert here
                rebuiltIndex.ensureCapacity(id.toString().length() + 1);

                rebuiltIndex.append(id.toString());
                rebuiltIndex.append(',');

                inserted = true;
            }

            // Avoid doubling in size
            rebuiltIndex.ensureCapacity(idString.length() + 1);

            rebuiltIndex.append(idString);
            rebuiltIndex.append(',');
        }

        if (!inserted) {
            rebuiltIndex.ensureCapacity(id.toString().length() + 1);

            rebuiltIndex.append(id.toString());
            rebuiltIndex.append(',');
        }

        rebuiltIndex.setCharAt(rebuiltIndex.length() - 1, ']');
        return rebuiltIndex.toString();
    }

    /**
     * Delete from a index containing JSONObject.
     * Expects each JSONObject to have a long property "id".
     *
     * @param id  of the json object
     * @param jsonArrayIterator for accessing the json array
     * @return new json after delete
     */
    public static String deleteFromJSONIndex(Long id, JSONArrayIterator jsonArrayIterator) {
        StringBuilder rebuiltIndex = new StringBuilder("[");

        while (jsonArrayIterator.hasNext()) {
            JSONObject currentIndexedJSON = (JSONObject) jsonArrayIterator.next();
            Long currentId = currentIndexedJSON.getLong("id");
            // Check if we're interested in this template
            if (!currentId.equals(id)) {
                String indexedSessionJSONString = currentIndexedJSON.toString();
                // Avoid doubling in size
                rebuiltIndex.ensureCapacity(indexedSessionJSONString.length() + 1);

                rebuiltIndex.append(indexedSessionJSONString);
                rebuiltIndex.append(',');
            }
        }
        // Replace the last character with a ]
        if (rebuiltIndex.length() <= 1) {
            return "[]";
        } else {
            rebuiltIndex.setCharAt(rebuiltIndex.length() - 1, ']');
            return rebuiltIndex.toString();
        }

    }

    /**
     * Add to a index containing JSONObject.
     * Expects each JSONObject to have a long property "id".
     *
     * @param json to add to list
     * @param jsonArrayIterator for the json array to add to
     * @return string representation of the array
     */
    public static String addToJSONIndex(JSONObject json, JSONArrayIterator jsonArrayIterator) {
        Long id = JSONKit.getLong(json, "id");
        String jsonString = json.toString();
        StringBuilder rebuiltIndex = new StringBuilder("[");
        if (!jsonArrayIterator.hasNext()) {
            rebuiltIndex.append(jsonString);
            rebuiltIndex.append("]");
            return rebuiltIndex.toString();
        }

        boolean inserted = false;

        while (jsonArrayIterator.hasNext()) {
            JSONObject currentIndexedJSON = JSONKit.to(String.valueOf(jsonArrayIterator.next()));
            Long currentId = JSONKit.getLong(currentIndexedJSON, "id");

            if (!inserted && currentId.equals(id)) {
                // Need to insert the more recent one over the top
                currentIndexedJSON = json;
                inserted = true;
            }
            if (!inserted && currentId < id) {
                // Insert here
                rebuiltIndex.ensureCapacity(jsonString.length() + 1);

                rebuiltIndex.append(jsonString);
                rebuiltIndex.append(',');

                inserted = true;
            }

            String currentIndexedJSONString = currentIndexedJSON.toString();
            // Avoid doubling in size
            rebuiltIndex.ensureCapacity(currentIndexedJSONString.length() + 1);

            rebuiltIndex.append(currentIndexedJSONString);
            rebuiltIndex.append(',');
        }

        if (!inserted) {
            rebuiltIndex.ensureCapacity(jsonString.length() + 1);

            rebuiltIndex.append(jsonString);
            rebuiltIndex.append(',');
        }

        rebuiltIndex.setCharAt(rebuiltIndex.length() - 1, ']');
        return rebuiltIndex.toString();
    }
}
