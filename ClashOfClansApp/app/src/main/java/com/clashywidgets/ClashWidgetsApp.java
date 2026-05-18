package com.clashywidgets;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Custom Application class.
 *
 * Responsibilities:
 *   1. Create the "Upgrade Alarms" notification channel on first run (required API 26+).
 *      Channels only need to be created once; calling createNotificationChannel on an
 *      existing channel is a no-op, so it's safe to call every launch.
 */
public class ClashWidgetsApp extends Application {

    public static final String ALARM_CHANNEL_ID = "upgrade_alarms";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    ALARM_CHANNEL_ID,
                    getString(R.string.alarm_channel_name),
                    NotificationManager.IMPORTANCE_HIGH   // heads-up notification
            );
            channel.setDescription("Fires 1 minute before an upgrade finishes.");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
