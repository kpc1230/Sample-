package com.thed.zephyr.capture.model.jira;

import java.io.Serializable;
import java.net.URI;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class CaptureResolution implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3913738511403407806L;
	
	final private Long id;
	
	final private String name;
	
	final private URI uri;
	
	public CaptureResolution(Long id, String name, URI uri) {
		this.id = id;
		this.name = name;
		this.uri = uri;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public URI getUri() {
		return uri;
	}
	
}
