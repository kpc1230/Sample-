package com.atlassian.excalibur.web.util;


import com.atlassian.jira.ComponentManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.util.ClassLoaderUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A helper to invoke methods and otherwise smooth out JIRA 4 to JIRA 5 incompatibility changes
 */
public class ReflectionKit<T> {
    /**
     * This is returned out of the reflection methods to enabled searching with one set of Class
     * parameters and invocation with another set of run time parameters.
     */
    public interface CallableMethod<T> {

        <T> T call(Object... params);
    }

    /**
     * This will invoke the named method with the specified parameters.  It will throw runtime exceptions if
     * none of this works so test well and test often!
     *
     * @param instance   the instance object to invoke on
     * @param methodName the method name to invoke
     * @param paramTypes the class of the parameters of the method to find
     * @return the result of invoking the method
     * @throws RuntimeException if none of this can be done.  The onus is on the caller to get it right! (TM)
     */
    public static <T> CallableMethod<T> method(Object instance, String methodName, final Class<?>... paramTypes) {
        return invokeMethodImpl(false, instance, methodName, null, paramTypes);
    }

    /**
     * This will OPTIONALL invoke the named method with the specified parameters.  It will throw runtime exceptions if
     * none of this works so test well and test often!
     *
     * @param instance                 the instance object to invoke on
     * @param methodName               the method name to invoke
     * @param defaultValueIfNotPresent the value to return if the method is not present
     * @param paramTypes               the class of the parameters of the method to find
     * @param <T>                      the type of the return type
     * @return the result of invoking the method
     * @throws RuntimeException if none of this can be done.  The onus is on the caller to get it right! (TM)
     */
    public static <T> CallableMethod<T> methodWithDefault(Object instance, String methodName, T defaultValueIfNotPresent,
                                                          final Class<?>... paramTypes) {
        return invokeMethodImpl(true, instance, methodName, defaultValueIfNotPresent, paramTypes);
    }

    /**
     * This will OPTIONALL invoke the named method with the specified parameters on a named JIRA PICO component.  It will throw runtime exceptions if
     * none of this works so test well and test often!
     *
     * @param componentClassName       the class name to load out of PICO
     * @param methodName               the method name to invoke
     * @param defaultValueIfNotPresent the value to return if the method is not present
     * @param paramTypes               the class of the parameters of the method to find
     * @param <T>                      the type of the return type
     * @return the result of invoking the method
     * @throws RuntimeException if none of this can be done.  The onus is on the caller to get it right! (TM)
     */
    public static <T> CallableMethod<T> methodOfJIRAComponent(String componentClassName, String methodName, T defaultValueIfNotPresent, final Class<?>... paramTypes) {
        try {
            Class desiredClass = ClassLoaderUtils.loadClass(componentClassName, ReflectionKit.class);
            //noinspection unchecked
            Object instance = ComponentManager.getComponent(desiredClass);
            return invokeMethodImpl(true, instance, methodName, defaultValueIfNotPresent, paramTypes);
        } catch (ClassNotFoundException e) {
            return new DefaultedCallableMethod<T>(defaultValueIfNotPresent);
        }
    }


    /**
     * The various module descriptors in JIRA have changed enough so that the getHtml() method needs to be called reflectively.
     *
     * @param moduleDescriptor the ModuleDescriptor to call getHtml() on
     * @param viewName         the view name
     * @param params           the starting parameters to push into the Velocity context
     * @return the Velocity rendered String
     */
    public static String getHtml(ModuleDescriptor moduleDescriptor, String viewName, Map<String, ?> params) {
        return ReflectionKit.method(moduleDescriptor, "getHtml", String.class, Map.class).call(viewName, params);
    }

    private static <T> CallableMethod<T> invokeMethodImpl(final boolean optionalMethodCall, final Object instance, final String methodName, final T defaultValueIfNotPresent, final Class<?>... paramTypes) {
        try {
            final Method method = notNull("instance", instance).getClass().getMethod(notNull("methodName", methodName), paramTypes);
            return new CallableMethod<T>() {
                @Override
                public <T> T call(Object... params) {
                    try {
                        Object returnObject = method.invoke(instance, params);
                        //noinspection unchecked
                        return (T) returnObject;
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("Egregious runtime error - could not invoke " + methodName, e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Egregious runtime error - could not access " + methodName, e);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            if (optionalMethodCall) {
                return new DefaultedCallableMethod<T>(defaultValueIfNotPresent);
            }
            throw new RuntimeException("Egregious coding error - there must be a method called " + methodName, e);
        }
    }

    private static class DefaultedCallableMethod<T> implements CallableMethod<T> {
        private final T defaultIfNotPresent;

        private DefaultedCallableMethod(T defaultIfNotPresent) {
            this.defaultIfNotPresent = defaultIfNotPresent;
        }

        @Override
        public <T> T call(Object... params) {
            //noinspection unchecked
            return (T) defaultIfNotPresent;
        }
    }

}
