package com.thed.zephyr.capture.model.view;

import java.io.Serializable;

public class IssueSearchDto  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4981058965225095440L;
	private Long id;
	private String key;
	private String iconUrl;
	private String summary;
	
	public IssueSearchDto(Long id, String key, String iconUrl, String summary) {
		this.id = id;
		this.key = key;
		this.iconUrl = iconUrl;
		this.summary = summary;
	}

	public Long getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public String getSummary() {
		return summary;
	}
	
}