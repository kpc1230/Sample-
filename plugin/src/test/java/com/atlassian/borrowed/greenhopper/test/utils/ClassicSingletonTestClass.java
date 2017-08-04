/**
 *
 */
package com.atlassian.borrowed.greenhopper.test.utils;

public class ClassicSingletonTestClass {
    private static ClassicSingletonTestClass instance;
    private boolean constructorCalled = false;

    public static ClassicSingletonTestClass getInstance() {
        if (instance == null) instance = new ClassicSingletonTestClass();
        return instance;
    }

    private ClassicSingletonTestClass() {
        constructorCalled = true;
    }

    public boolean isConstructorCalled() {
        return constructorCalled;
    }
}