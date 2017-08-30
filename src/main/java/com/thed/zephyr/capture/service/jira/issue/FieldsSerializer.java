package com.thed.zephyr.capture.service.jira.issue;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Serialises IssueFields, with the contents of the {@code fields} map at the same level as the other fields.
 */
public class FieldsSerializer extends JsonSerializer<IssueFields> {

    public FieldsSerializer() {
    }

    @Override
    public void serialize(final IssueFields issueFields, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (issueFields != null) {
            jgen.writeStartObject();
            serializeSystemFields(issueFields, jgen, provider);
            serializeCustomFields(issueFields, jgen, provider);
            jgen.writeEndObject();
        }
    }

    /**
     * Serialises the system fields.
     */
    protected void serializeSystemFields(final IssueFields issueFields, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException {
        for (Field field : issueFields.getClass().getFields()) {
            // only serialise public or annotated fields
            if (isPublic(field) || field.getAnnotation(JsonProperty.class) != null) {
                try {
                    Object fieldValue = field.get(issueFields);
                    if (fieldValue != null) {
                        provider.defaultSerializeField(field.getName(), fieldValue, jgen);
                    }
                } catch (IllegalAccessException e) {
                    throw new JsonGenerationException("Error reading field '" + field.getName() + "'", e);
                }
            }
        }
    }

    private void serializeCustomFields(IssueFields issueFields, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (issueFields.fields != null) {
            for (Map.Entry<String, String[]> customField : issueFields.fields.entrySet()) {
                jgen.writeArrayFieldStart(customField.getKey());
                for (String string : customField.getValue()) {
                    jgen.writeString(string);
                }
                jgen.writeEndArray();
            }
        }
    }

    protected static boolean isPublic(Field field) {
        return (field.getModifiers() & Modifier.PUBLIC) != 0;
    }
}
