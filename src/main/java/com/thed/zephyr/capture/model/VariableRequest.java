package com.thed.zephyr.capture.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class VariableRequest {
	@Size(min = 1, max = 200)
	private String name;
	@NotNull
	private String value;
	private String id;
	private String ownerName;

	public VariableRequest() {
		super();
	}

	public VariableRequest(String name, String value, String id, String ownerName) {
		super();
		this.name = name;
		this.value = value;
		this.id = id;
		this.ownerName = ownerName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
}
