package com.atlassian.borrowed.greenhopper.jira;

import com.atlassian.jira.ComponentManager;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;

/**
 * <p>
 * JIRA publishes components through ContainerRegistrar. Unfortunately, some components are marked as "internal" and can't be injected by
 * Spring. To work around this and keep static calls from being scattered all around, this PostProcessors can be used together with
 * {@link JIRAResource} annotation.
 * </p>
 * <p>
 * Declared in spring.xml, this runs as another PostProcessors after the default Spring ones. It inspects container-managed beans and tries to resolve
 * annotated fields.
 * </p>
 * <p> see http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#beans-factory-extension-bpp for a detailed documentation on PostProcessors.</p>
 *
 * @author ahennecke
 */
public class JIRAResourceAnnotationBeanPostProcessor implements BeanPostProcessor {
    private final Logger log = Logger.getLogger(getClass());

    /**
     * We hook in before the initialisation methods (like afterPropertiesSet) are called, so injection is complete if someone needs it at this point.
     */
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        // go through all fields in the given type.
        ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(JIRAResource.class)) {
                    // try to resolve the internal dependency through ComponentManager. Will be null if there is no such bean.
                    Object jiraResource = ComponentManager.getComponentInstanceOfType(field.getType());
                    if (jiraResource == null) {
                        String message = "Unable to inject JIRA managed component of type " + field.getType() + ": No such component defined";
                        log.error(message);
                        throw new BeanCreationException(beanName, message);
                    } else {
                        // field injection. Beware of SecurityManager.
                        ReflectionUtils.makeAccessible(field);
                        field.set(bean, jiraResource);
                    }
                }
            }
        });

        return bean;
    }

    /**
     * Nothing for us to do here.
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
