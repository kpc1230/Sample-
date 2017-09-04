package com.thed.zephyr.capture.model;

public class LightTag {
    private String id;
	private String name;

	public LightTag() {
		super();
	}
	public LightTag(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
}
