package com.ns.gamerecorder;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class App extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "com.ns.notification.1";
    public static final String NOTIFICATION_CHANNEL_NAME = "capture notification";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "controls the screen capture";
    public static final String SHARED_PREF_KEY = "com.ns.config_index";

    // selector keys
    public static final String VIDEO_RESOLUTION_INDEX = "1";
    public static final String VIDEO_ENCODER_INDEX = "2";
    public static final String AUDIO_ENCODER_INDEX = "3";
    public static final String FRAME_RATE_INDEX = "4";
    public static final String BITRATE_INDEX = "5";
    public static final String OUTPUT_FORMAT = "6";

    public RecorderConfig config;
    public SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();

        config = new RecorderConfig();
        SRef.config = config;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        pref = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    // get the data out from shared pref
    // this is used to get selected value
    // for the collection defined in RecorderConfig class
    public int getSelectIndex(String key) {
        return pref.getInt(key, 0);
    }

    public void putSelectIndex(String key, int index) {
        pref.edit().putInt(key, index).apply();
    }


    // create notification channel for android versions starting from O
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel =
                new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);

        channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
