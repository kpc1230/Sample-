package com.thed.zephyr.capture.rest;

import com.thed.zephyr.capture.events.GenericAnalyticsEvent;
import com.thed.zephyr.capture.rest.model.EmptyBean;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

@Path("/analytics")
public class AnalyticsResource extends BonfireRestResource {
    @JIRAResource
    private EventPublisher eventPublisher;

    public AnalyticsResource() {
        super(AnalyticsResource.class);
    }

    /**
     * IMPORTANT: This method must always return 200OK. If anything goes wrong, it should just stop what it's doing and return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createSessionRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse == null) {
                    // Validate the JSON object
                    final JSONObject jsonData;
                    try {
                        jsonData = new JSONObject(requestBody);
                    } catch (JSONException e) {
                        log.debug("JSON exception when trying to parse requestBody.");
                        return ok(new EmptyBean());
                    }
                    JSONArray events = jsonData.getJSONArray("events");
                    for (JSONArray data : events.arrays()) {
                        GenericAnalyticsEvent event = parseAnalyticsData(data);
                        publishSilently(event);
                    }

                    // TODO: Check whether it's used via houston.internal.atlassian.com
                    for (JSONObject data : events.objects()) {
                        GenericAnalyticsEvent event = parseDeprecatedAnalyticsData(data);
                        publishSilently(event);
                    }
                } else {
                    log.debug("User is anonymous or license is invalid");
                }

                return ok(new EmptyBean());
            }
        });
    }

    /**
     * Parses events from JSON array, like:
     * ["jira.capture.attachments.add",{"source":"extension", "newIssue":"true"}]
     *
     * @param data json array
     * @return analytics event
     */
    @VisibleForTesting
    GenericAnalyticsEvent parseAnalyticsData(final JSONArray data) {
        try {
            if (data.length() != 0) {
                final GenericAnalyticsEvent.Builder builder = GenericAnalyticsEvent.builder();
                final String eventName = data.getString(0);
                builder.name(eventName);

                if (data.length() > 1) {
                    final Object obj = data.get(1);
                    if (!((Object) JSONObject.NULL).equals(obj)) {
                        if (obj instanceof JSONObject) {
                            // here we must have 2 elements array, where second element is JSON
                            final JSONObject jsonObject = ((JSONObject) obj);
                            builder.source(getStringHelper(jsonObject, "source"))
                                    .action(getStringHelper(jsonObject, "action"))
                                    .dataSource(getStringHelper(jsonObject, "dataSource"))
                                    .count(getLongHelper(jsonObject, "count"))
                                    .browser(getStringHelper(jsonObject, "browser"))
                                    .newIssue(getBooleanOrNull(getStringHelper(jsonObject, "newIssue")));
                        }
                    }
                }
                return builder.build();
            }
        } catch (JSONException e) {
            log.debug("JSON exception when trying to parse event data. data=" + data);
        }
        return null;
    }

    /**
     * Older clients, prior to 2.8.0 used different object format, like:
     * {"category":"bonfire.xxx","action":"Add Attachment","label":"Drag And Drop"}
     *
     * @param data raw json data
     * @return analytics event
     */
    @Deprecated
    @VisibleForTesting
    GenericAnalyticsEvent parseDeprecatedAnalyticsData(final JSONObject data) {
        try {
            return GenericAnalyticsEvent.builder()
                    .name(getStringHelper(data, "category"))
                    .action(getStringHelper(data, "action"))
                    .label(getStringHelper(data, "label"))
                    .build();
        } catch (JSONException e) {
            log.debug("JSON exception when trying to parse deprecated event data. data=" + data);
        }
        return null;
    }

    private Boolean getBooleanOrNull(String rawValue) {
        if (StringUtils.isEmpty(rawValue)) {
            return null;
        }
        return Boolean.parseBoolean(rawValue);
    }

    private void publishSilently(@Nullable GenericAnalyticsEvent event) {
        if (event != null) {
            eventPublisher.publish(event);
        }
    }

    @Nullable
    private String getStringHelper(JSONObject json, String key) {
        if (!json.has(key)) {
            return null;
        } else {
            final String value = json.getString(key);
            return StringUtils.isEmpty(value) ? null : value;
        }
    }

    @Nullable
    private Long getLongHelper(JSONObject json, String key) {
        if (!json.has(key)) {
            return null;
        } else {
            final String value = json.getString(key);
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            if (!StringUtils.isNumeric(value)) {
                return null;
            }

            return Long.parseLong(value);

        }
    }

}
