package com.atlassian.borrowed.greenhopper.service;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.apache.commons.lang.Validate.notNull;

/**
 * <p>
 * Implementation of the PersistenceService interface.
 * </p>
 * <p>
 * Wraps JiraPropertySetFactory for persistence.
 * </p>
 */
@Service(PersistenceService.SERVICE)
@SuppressWarnings("unused")
public class PersistenceServiceImpl implements PersistenceService {
    private final Logger log = Logger.getLogger(getClass());

    @JIRAResource
    private JiraPropertySetFactory jiraPropertySetFactory;

    /**
     * XStream object used to serialize to / deserialize from Strings.
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private final XStream xstream = new XStream(new Sun14ReflectionProvider());

    /**
     * Set a Long property.
     */
    public void setLong(String entityName, Long entityId, String key, Long value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setLong(key, value);
    }

    /**
     * Get a Long property.
     */
    public Long getLong(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getLong(key) : null;
    }

    /**
     * Set a String property.
     */
    public void setString(String entityName, Long entityId, String key, String value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setString(key, value);
    }

    /**
     * Get a String property.
     */
    public String getString(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getString(key) : null;
    }

    public void setText(String entityName, Long entityId, String key, String value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setText(key, value);
    }

    public String getText(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getText(key) : null;
    }

    /**
     * Set a Double property.
     */
    public void setDouble(String entityName, Long entityId, String key, Double value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setDouble(key, value);
    }

    /**
     * Get a Double property.
     */
    public Double getDouble(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getDouble(key) : null;
    }

    public void setBoolean(String entityName, Long entityId, String key, Boolean value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setBoolean(key, value);
    }

    public Boolean getBoolean(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getBoolean(key) : null;
    }

    /**
     * Returns a map or null if not set
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getData(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        // Fetch the value
        String serialisedData = getPropertySet(entityName, entityId).getText(key);
        if (serialisedData == null) {
            return null;
        }

        // convert the data object into a map
        return (Map<String, Object>) xstream.fromXML(serialisedData);
    }

    /**
     * Converts the provided data map into a text property and stores it under key.
     */
    public void setData(String entityName, Long entityId, String key, Map<String, Object> data) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(data);

        // serialize the map
        String serializedData = xstream.toXML(data);

        // then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    /**
     * Returns a map or null if not set
     */
    @SuppressWarnings("unchecked")
    public Map<Long, Object> getDataLongKey(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        // Fetch the value
        String serializedData = getPropertySet(entityName, entityId).getText(key);
        if (serializedData == null) {
            return null;
        }

        // convert the data object into a map
        return (Map<Long, Object>) xstream.fromXML(serializedData);
    }

    /**
     * Converts the provided data map into a text property and stores it under key.
     */
    public void setDataLongKey(String entityName, Long entityId, String key, Map<Long, Object> data) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(data);

        // serialize the map
        String serializedData = xstream.toXML(data);

        // then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    @SuppressWarnings("unchecked")
    public List<Object> getListData(String entityName, Long entityId, String key) {
        notNull(entityName, "entityName must not be null");
        notNull(entityId, "entityId must not be null");
        notNull(key, "key must not be null");

        // Fetch the value
        String serializedData = getPropertySet(entityName, entityId).getText(key);
        if (serializedData == null) {
            return null;
        }

        // convert the data object into a list
        return (List<Object>) xstream.fromXML(serializedData);
    }

    public void setListData(String entityName, Long entityId, String key, List<Object> data) {
        notNull(entityName, "entityName must not be null");
        notNull(entityId, "entityId must not be null");
        notNull(key, "key must not be null");
        notNull(data, "data must not be null");

        // serialize the list
        String serializedData = xstream.toXML(data);

        if (log.isDebugEnabled()) {
            log.debug("Storing list data in property set: " + entityName + ":" + entityId + " => " + key + ":" + serializedData);
        }

        // then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    /**
     * Get all keys defined for an entity name / entity id couple
     */
    @SuppressWarnings("rawtypes")
    public Set<String> getKeys(String entityName, Long entityId) {
        notNull(entityName);
        notNull(entityId);

        // fetch the keys
        Collection keys = getPropertySet(entityName, entityId).getKeys();
        if (keys.isEmpty()) {
            return Collections.emptySet();
        }

        // convert to a set of Strings
        Set<String> keySet = new HashSet<String>();
        for (Object key : keys) {
            keySet.add((String) key);
        }
        return keySet;
    }

    /**
     * Does a given key exist?
     */
    public boolean exists(String entityName, Long entityId, String key) {
        return getPropertySet(entityName, entityId).exists(key);
    }

    public JSONArrayIterator getJSONArrayIterator(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        // Fetch the value
        String serializedData = getPropertySet(entityName, entityId).getText(key);

        // convert the data object into a map
        try {
            return new JSONArrayIterator(serializedData);
        } catch (JSONException e) {
            log.error("Unable to create JSONArrayIterator", e);
            throw new RuntimeException(e);
        }
    }

    public void setJSONArray(String entityName, Long entityId, String key, JSONArray jsonArray) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(jsonArray);

        // Serialize the JSONArray
        String serializedData = jsonArray.toString();

        // Then store the value
        getPropertySet(entityName, entityId).setText(key, serializedData);
    }

    /**
     * Remove a property for a given entity name and entity id couple
     */
    public void delete(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        PropertySet propertySet = getPropertySet(entityName, entityId);
        removeProperty(propertySet, key);
    }

    /**
     * Remove all properties for an entity name, entity id couple
     */
    @SuppressWarnings("rawtypes")
    public void deleteAll(String entityName, Long entityId) {
        notNull(entityName);
        notNull(entityId);

        // remove all properties of this set
        PropertySet propertySet = getPropertySet(entityName, entityId);
        Collection keys = propertySet.getKeys();
        for (Object key : keys) {
            removeProperty(propertySet, (String) key);
        }
    }

    /**
     * Loads a PropertySet from the storage given a sequenceName/sequenceId mapping.
     *
     * @return a PropertySet for the given entityName and entityId.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private PropertySet getPropertySet(String entityName, Long entityId) {
        return jiraPropertySetFactory.buildCachingPropertySet(entityName, entityId, false);
    }

    /**
     * Removes a property. Silently swallows thrown exception
     */
    private void removeProperty(PropertySet propertySet, String key) {
        try {
            if (propertySet.exists(key)) propertySet.remove(key);
        } catch (PropertyException e) {
            log.warn(e, e);
        }
    }
}
