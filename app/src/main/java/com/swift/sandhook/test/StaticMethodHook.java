package com.swift.sandhook.test;

import android.util.Log;

public class StaticMethodHook {

    static {
        Log.e("StaticMethodHook", "xiawanli StaticMethodHook class static method is called");
    }

    public static void test() {
        Log.e("StaticMethodHook", "xiawanli test is called");
    }
}
