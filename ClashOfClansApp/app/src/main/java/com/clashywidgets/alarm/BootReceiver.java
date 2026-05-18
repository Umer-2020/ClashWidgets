package com.clashywidgets.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.repository.UpgradeRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BootReceiver — listens for BOOT_COMPLETED and ACTION_MY_PACKAGE_REPLACED.
 *
 * AlarmManager alarms are cleared when the device reboots or the app is updated.
 * This receiver re-schedules exact alarms for every UpgradeTask whose endTime
 * is still in the future, ensuring users still get notifications after a reboot.
 *
 * Declared in AndroidManifest with:
 *   <action android:name="android.intent.action.BOOT_COMPLETED" />
 *   <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
 * and requires RECEIVE_BOOT_COMPLETED permission (already in manifest).
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) &&
            !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        Log.d(TAG, "Boot/update detected — rescheduling active upgrade alarms.");

        // Use goAsync() so the BroadcastReceiver context stays alive while we
        // query the DB on a background thread.
        final PendingResult result = goAsync();

        executor.execute(() -> {
            try {
                UpgradeRepository repo = UpgradeRepository.getInstance(context);
                long now = System.currentTimeMillis();
                int rescheduled = 0;

                // Reschedule for all villages / types
                List<SlotWithTask> allSlots = repo.getAllSlotsWithTasksSync();
                for (SlotWithTask swt : allSlots) {
                    if (swt.task != null && swt.task.endTime > now) {
                        AlarmScheduler.scheduleAlarm(context, swt.task, swt.slot.name);
                        rescheduled++;
                    }
                }

                Log.d(TAG, "Rescheduled " + rescheduled + " alarm(s) after boot/update.");
            } finally {
                result.finish();
            }
        });
    }
}
