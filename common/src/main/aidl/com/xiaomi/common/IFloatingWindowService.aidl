// IFloatingWindowService.aidl
package com.xiaomi.common;

// Declare any non-default types here with import statements

interface IFloatingWindowService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    boolean getWindowStatus();
    void showWindow();
    void hideWindows();
}