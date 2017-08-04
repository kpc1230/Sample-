package com.atlassian.bonfire.util;

import com.atlassian.integrationtesting.runner.CompositeTestRunner;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import java.util.Set;

/**
 * This was copied from JIRA because it was removed in 5.2 This will eventually be updated into it's own standalone thing, but until then We will need
 * a copy here
 *
 * @author ezhang
 */
public class PageObjectsInjectorCopy {

    public static CompositeTestRunner.Composer compose(JiraTestedProduct product) {
        return CompositeTestRunner.compose().beforeTestClass(new InjectStatics(product.injector()))
                .beforeTestMethod(new InjectInstanceFields(product.injector()));
    }

    // TODO this is copied from AIT, should be reused!
    private static final class InjectStatics implements Function<CompositeTestRunner.BeforeTestClass, Void> {
        private final Injector injector;
        private static final Set<Class<?>> alreadyInjectedClasses = Sets.newHashSet();

        public InjectStatics(Injector injector) {
            this.injector = injector;
        }

        public Void apply(CompositeTestRunner.BeforeTestClass test) {
            synchronized (alreadyInjectedClasses) {
                if (!alreadyInjectedClasses.contains(test.testClass.getJavaClass())) {
                    injector.createChildInjector(new StaticInjectionModule(test.testClass.getJavaClass()));
                    alreadyInjectedClasses.add(test.testClass.getJavaClass());
                }
            }
            return null;
        }

        private static final class StaticInjectionModule extends AbstractModule {
            private final Class<?> testClass;

            public StaticInjectionModule(Class<?> testClass) {
                this.testClass = testClass;
            }

            @Override
            protected void configure() {
                requestStaticInjection(testClass);
            }
        }
    }

    private static final class InjectInstanceFields implements Function<CompositeTestRunner.BeforeTestMethod, Void> {
        private final Injector injector;

        public InjectInstanceFields(Injector injector) {
            this.injector = injector;
        }

        public Void apply(CompositeTestRunner.BeforeTestMethod test) {
            injector.injectMembers(test.target);
            return null;
        }
    }
}
