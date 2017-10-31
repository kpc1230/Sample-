package com.thed.zephyr.capture.service.jira.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.service.jira.GroupService;
import com.thed.zephyr.capture.util.JiraConstants;

@Service
public class GroupServiceImpl implements GroupService {
	
	@Autowired
    private AtlassianHostRestClients atlassianHostRestClients;

	@Override
	public List<Map<String, String>> findGroups(String query) {
		List<Map<String, String>> listOfGroups = new ArrayList<>(1);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String uri = host.getHost().getBaseUrl();
        URI targetUrl = UriComponentsBuilder.fromUriString(uri)
                .path(JiraConstants.REST_API_GROUP_PICKER)
                .queryParam("query", query == null ? "" : query)
                .queryParam("maxResults", 1000)
                .build()
                .encode()
                .toUri();

        JsonNode responseJsonNode = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, JsonNode.class);
        JsonNode groupsJsonNode = responseJsonNode.get("groups");
        groupsJsonNode.elements().forEachRemaining(jsoneNode -> {
        	String name = jsoneNode.get("name").asText();
        	Map<String, String> groupMap = new HashMap<>();
        	groupMap.put("text", name);
        	groupMap.put("value", name);
        	listOfGroups.add(groupMap);
        });
		return listOfGroups;
	}

}
