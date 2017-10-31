package com.thed.zephyr.capture.service.jira;

import java.util.List;
import java.util.Map;

public interface GroupService {
	
	List<Map<String, String>> findGroups(String query);

}
