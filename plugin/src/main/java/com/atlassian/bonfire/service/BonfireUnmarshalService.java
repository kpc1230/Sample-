package com.atlassian.bonfire.service;

import com.atlassian.bonfire.model.FavouriteTemplate;
import com.atlassian.bonfire.model.Template;
import com.atlassian.bonfire.model.Variable;
import com.atlassian.excalibur.model.Participant;
import com.atlassian.excalibur.model.ParticipantBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is here because model classes won't have enough information to unmarshal themselves from JSON and build themselves
 */

@Service(BonfireUnmarshalService.SERVICE)
public class BonfireUnmarshalService {
    public static final String SERVICE = "bonfire-bonfireunmarshalservice";

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService userService;

    public Participant getParticipantFromJSON(JSONObject json) {
        ApplicationUser user;
        DateTime timeJoined;
        DateTime timeLeft;

        try {
            timeJoined = ISODateTimeFormat.dateTime().parseDateTime((String) json.get(Participant.KEY_PARTICIPANT_JOINED));
            timeLeft = json.has(Participant.KEY_PARTICIPANT_LEFT) ? ISODateTimeFormat.dateTime().parseDateTime(
                    (String) json.get(Participant.KEY_PARTICIPANT_LEFT)) : null;
            user = userService.safeGetUserByKey((String) json.get(Participant.KEY_PARTICIPANT_USER));
        } catch (JSONException e) {
            return null;
        }
        return new ParticipantBuilder(user).setTimeJoined(timeJoined).setTimeLeft(timeLeft).build();
    }

    public FavouriteTemplate getFavTemplateFromJSON(JSONObject json) {
        try {
            return new FavouriteTemplate(new DateTime(json.getLong(FavouriteTemplate.KEY_FAVOURITE_TEMPLATE_TIME)),
                    json.getBoolean(FavouriteTemplate.KEY_FAVOURITE_TEMPLATE_IS));
        } catch (JSONException e) {
            return FavouriteTemplate.INVALID;
        }
    }

    public Template getTemplateFromJSON(JSONObject json) {
        boolean sharedTemp;
        String jsonSourceTemp;
        List<Variable> variablesTemp = Collections.emptyList();
        DateTime timeCreatedTemp;
        DateTime timeUpdatedTemp;
        DateTime timeVariablesUpdatedTemp;
        String ownerNameTemp;
        Long projectIdTemp;
        Long idTemp;
        try {
            idTemp = json.getLong(Template.KEY_TEMPLATE_ID);
            projectIdTemp = json.getLong(Template.KEY_TEMPLATE_PROJECT_ID);
            String userKey = (String) json.get(Template.KEY_TEMPLATE_OWNER);
            ownerNameTemp = userService.safeGetUserByKey(userKey).getName();
            jsonSourceTemp = (json.get(Template.KEY_TEMPLATE_SOURCE)).toString();
            if (json.has(Template.KEY_TEMPLATE_VARIABLES)) {
                JSONArray variablesArray = json.getJSONArray(Template.KEY_TEMPLATE_VARIABLES);
                variablesTemp = new ArrayList<Variable>(variablesArray.length());
                for (int i = 0; i < variablesArray.length(); i++) {
                    variablesTemp.add(Variable.create(variablesArray.getJSONObject(i)));
                }
            }
            timeCreatedTemp = ISODateTimeFormat.dateTime().parseDateTime((String) json.get(Template.KEY_TEMPLATE_TIME_TEMPLATE_CREATED));
            timeUpdatedTemp = ISODateTimeFormat.dateTime().parseDateTime((String) json.get(Template.KEY_TEMPLATE_TIME_TEMPLATE_UPDATED));
            timeVariablesUpdatedTemp = ISODateTimeFormat.dateTime().parseDateTime((String) json.get(Template.KEY_TEMPLATE_TIME_VARS_UPDATED));
            sharedTemp = Boolean.valueOf((String) json.get(Template.KEY_TEMPLATE_SHARED));
        } catch (JSONException e) {
            // Create an empty template, with known ID which is different from the json stored ID to check against.
            return Template.EMPTY;
        } catch (ClassCastException e) {
            // Data isn't in the shape we expect
            return Template.EMPTY;
        }

        return new Template(idTemp, projectIdTemp, ownerNameTemp, jsonSourceTemp, variablesTemp, timeCreatedTemp, timeUpdatedTemp,
                timeVariablesUpdatedTemp, sharedTemp);
    }
}
