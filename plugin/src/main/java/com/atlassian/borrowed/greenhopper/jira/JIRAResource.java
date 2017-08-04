package com.atlassian.borrowed.greenhopper.jira;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for an internal JIRA resource that should be DI'ed. See {@link JIRAResourceAnnotationBeanPostProcessor} for details.
 *
 * @author ahennecke
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JIRAResource {
    /**
     * Since we don't really have an idea how JIRA resources are published, autowiring by type is normally used. In case we do have a name, it can be
     * specified here.
     */
    String name() default "";
}
