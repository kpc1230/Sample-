package com.atlassian.excalibur.service.dao;

import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * DAO layer for global Excalibur properties. These are stored as one value per PropertySet record.
 *
 * @author ahennecke
 */
@Service(PropertyDao.SERVICE)
public class PropertyDao {
    public static final String SERVICE = "excalibur-propertyDao";

    private static final String KEY_EX_PROPS = "Excalibur.properties";
    private static final long GLOBAL_ENTITY_ID = 1l;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    /**
     * @return the Boolean value for the given property key, or null
     */
    public Boolean getBooleanProperty(String key) {
        return persistenceService.getBoolean(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Boolean value for the given property key
     */
    public void setBooleanProperty(String key, Boolean value) {
        persistenceService.setBoolean(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @return the Long value for the given property key, or null
     */
    public Long getLongProperty(String key) {
        return persistenceService.getLong(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set the Long value for the given property key
     */
    public void setLongProperty(String key, Long value) {
        persistenceService.setLong(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * Set a string value for a given property key
     */
    public void setStringProperty(String key, String value) {
        persistenceService.setString(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @return the String value for the given property key, or null
     */
    public String getStringProperty(String key) {
        return persistenceService.getString(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set a textvalue for a given property key
     */
    public void setTextProperty(String key, String value) {
        persistenceService.setText(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @return the Text value for the given property key, or null
     */
    public String getTextProperty(String key) {
        return persistenceService.getText(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set a map value for a given property key
     */
    public void setMapProperty(String key, Map<String, Object> value) {
        persistenceService.setData(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @return the map value for the given property key, or null
     */
    public Map<String, Object> getMapProperty(String key) {
        return persistenceService.getData(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Set a map value for a given property key
     */
    public void setLongMapProperty(String key, Map<Long, Object> value) {
        persistenceService.setDataLongKey(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key, value);
    }

    /**
     * @return the map value for the given property key, or null
     */
    public Map<Long, Object> getLongMapProperty(String key) {
        return persistenceService.getDataLongKey(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }

    /**
     * Deletes the named property
     *
     * @param key the property to delete
     */
    public void deleteProperty(String key) {
        persistenceService.delete(KEY_EX_PROPS, GLOBAL_ENTITY_ID, key);
    }
}
