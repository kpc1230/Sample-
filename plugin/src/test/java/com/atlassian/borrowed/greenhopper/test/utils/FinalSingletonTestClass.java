/**
 *
 */
package com.atlassian.borrowed.greenhopper.test.utils;

public class FinalSingletonTestClass {
    private static FinalSingletonTestClass INSTANCE = new FinalSingletonTestClass();
    ;
    private boolean constructorCalled = false;

    public static FinalSingletonTestClass getInstance() {
        return INSTANCE;
    }

    private FinalSingletonTestClass() {
        constructorCalled = true;
    }

    public boolean isConstructorCalled() {
        return constructorCalled;
    }
}