package com.clashywidgets.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.clashywidgets.ClashWidgetsApp;
import com.clashywidgets.R;
import com.clashywidgets.ui.main.MainActivity;

/**
 * AlarmReceiver — fires when an exact AlarmManager alarm triggers.
 *
 * Responsibilities:
 *   1. Read the task name from the intent extras.
 *   2. Build a high-priority (heads-up) notification telling the user the upgrade
 *      is finishing in 1 minute.
 *   3. Post the notification with the task ID as the notification ID so multiple
 *      concurrent upgrades each get their own independent notification.
 *
 * Sound: Uses the system default notification sound (set via
 *   NotificationCompat.Builder.setDefaults). When the user provides a custom
 *   "upgrade finished" sound file, it can be placed in res/raw/ and referenced
 *   via a Uri on the NotificationChannel (Phase 6 polish).
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        int    taskId   = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1);
        String taskName = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME);
        String slotName = intent.getStringExtra(AlarmScheduler.EXTRA_SLOT_NAME);

        if (taskId == -1) {
            Log.w(TAG, "AlarmReceiver fired with no task ID — ignoring.");
            return;
        }

        if (taskName == null || taskName.isEmpty()) {
            taskName = (slotName != null) ? slotName : "An upgrade";
        }

        Log.d(TAG, "Alarm fired for task " + taskId + " — " + taskName);
        postNotification(context, taskId, taskName);
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private void postNotification(Context context, int taskId, String taskName) {
        // Tap notification → open app
        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openAppPi = PendingIntent.getActivity(
                context, taskId, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String body = String.format(
                context.getString(R.string.alarm_notification_body), taskName);

        // Full-screen Intent -> AlarmActivity
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtra(AlarmScheduler.EXTRA_TASK_NAME, taskName);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, taskId, fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, ClashWidgetsApp.ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.alarm_notification_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setContentIntent(openAppPi)
                // Gold accent colour matching the app theme
                .setColor(0xFFF0A500);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            // Use taskId as notification ID → multiple upgrades = multiple notifications
            nm.notify(taskId, builder.build());
        }
    }
}
