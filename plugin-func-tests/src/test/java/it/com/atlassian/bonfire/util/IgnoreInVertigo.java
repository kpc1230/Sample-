package it.com.atlassian.bonfire.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to accompany the test rule IgnoreInVertigoRule.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IgnoreInVertigo {
    /**
     * The optional reason why the test is ignored.
     */
    String value() default "";
}