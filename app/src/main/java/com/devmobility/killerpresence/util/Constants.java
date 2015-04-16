package com.devmobility.killerpresence.util;

public interface Constants {

    public static final String APP_PREFS = "cht-bluetooth-app-prefs";
    public static final String PREFS_BT_MAC_ADDRESS = "prefs-br-mac-address";

    public static final String LAUNCH_PACKAGE_NAME = "com.example.test";
    public static final String LAUNCH_APP_NAME = ".MainActivity";

    public static final long BTLE_RECONNECT_INITIAL_DELAY = 10000; // milliseconds
    public static final long BTLE_RECONNECT_REPEAT_DELAY = 5000; // milliseconds

    public static double THRESHOLD = 0.0;

    public static final String PREFS_NEAR_THRESHOLD = "prefs-near-threshold";
    public static final String PREFS_FAR_THRESHOLD = "prefs-far-threshold";
}