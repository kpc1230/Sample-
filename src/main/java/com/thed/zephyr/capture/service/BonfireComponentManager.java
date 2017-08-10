package com.thed.zephyr.capture.service;

public interface BonfireComponentManager {
    public static final String SERVICE = "bonfire-ComponentManager";

    /**
     * This can be called to intatiate a new auto-wired instance of clazz
     *
     * @param clazz the class of the component to instatiate
     * @param <T>   the type of result
     * @return the resultant T
     */
    <T> T instatiateComponent(final Class<T> clazz);

}
