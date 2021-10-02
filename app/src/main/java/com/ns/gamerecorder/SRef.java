package com.ns.gamerecorder;

public class SRef {
    public static MainActivity hostActivity; // initialized in the MainActivity.onCreate();
    public static CaptureService captureService; // initialized in the CaptureService.onCreate()
    public static int device_width; // initialized in App class
    public static int device_height; // initialized in App class
    public static RecorderConfig config;  // init in the MainActivity.onCreate()
}
