package com.clashywidgets.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.clashywidgets.data.db.UpgradeTask;

/**
 * Utility class that schedules and cancels exact AlarmManager alarms.
 *
 * Strategy:
 *   - One alarm per active UpgradeTask, keyed by task.id (used as PendingIntent request code).
 *   - Alarm fires at  endTime − LEAD_MILLIS  (1 minute before upgrade completes).
 *   - If the alarm time is already in the past (task is almost done), we fire the
 *     notification immediately by scheduling for "now + 1 second".
 *
 * API considerations:
 *   - API 31+: USE_EXACT_ALARM (normal permission, auto-granted) is declared in the manifest.
 *     We use setExactAndAllowWhileIdle() which works even in Doze mode.
 *   - On API 31+, the app ALSO declares SCHEDULE_EXACT_ALARM as a fallback for older targetSdk.
 *     canScheduleExactAlarms() is checked before scheduling; if denied, we degrade to
 *     setAndAllowWhileIdle() which may be delayed by up to 15 min in Doze.
 */
public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    /** How far in advance (ms) the alarm fires before upgrade completion. */
    public static final long LEAD_MILLIS = 60_000L;  // 1 minute

    /** Extra keys put on the AlarmReceiver Intent. */
    public static final String EXTRA_TASK_ID     = "task_id";
    public static final String EXTRA_TASK_NAME   = "task_name";
    public static final String EXTRA_SLOT_NAME   = "slot_name";

    // ── Schedule ─────────────────────────────────────────────────────────────

    /**
     * Schedules an exact alarm that fires 1 minute before {@code task.endTime}.
     *
     * @param context  Application context.
     * @param task     The active UpgradeTask. Must have a valid id and endTime.
     * @param slotName Human-readable slot name (e.g. "Home Builder 1") used in the notification.
     */
    public static void scheduleAlarm(Context context, UpgradeTask task, String slotName) {
        if (task == null || task.endTime <= 0) return;

        long alarmAt = task.endTime - LEAD_MILLIS;
        // If already past, fire almost immediately so the user still sees the notification.
        if (alarmAt < System.currentTimeMillis()) {
            alarmAt = System.currentTimeMillis() + 1_000L;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = buildPendingIntent(context, task, slotName);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Permission not granted — fall back to inexact (Doze-aware but possibly delayed).
                Log.w(TAG, "SCHEDULE_EXACT_ALARM not granted; using inexact alarm for task " + task.id);
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pi);
            } else {
                // Exact alarm — fires precisely even in Doze.
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pi);
                Log.d(TAG, "Scheduled exact alarm for task " + task.id + " at " + alarmAt);
            }
        } catch (SecurityException e) {
            // Last-resort fallback if the permission is revoked mid-session.
            Log.e(TAG, "SecurityException scheduling alarm; falling back to inexact.", e);
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pi);
        }
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    /**
     * Cancels a previously scheduled alarm for the given task ID.
     * Safe to call even if no alarm was scheduled.
     */
    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        PendingIntent pi = buildCancelIntent(context, taskId);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
            Log.d(TAG, "Cancelled alarm for task " + taskId);
        } else {
            Log.d(TAG, "No alarm existed to cancel for task " + taskId);
        }
    }

    // ── PendingIntent helpers ─────────────────────────────────────────────────

    /**
     * Builds the PendingIntent delivered to AlarmReceiver when the alarm fires.
     * The task ID is used as the request code so each task gets a unique intent.
     */
    private static PendingIntent buildPendingIntent(Context context, UpgradeTask task,
                                                    String slotName) {
        String displayName = (task.buildingName != null && !task.buildingName.isEmpty())
                ? task.buildingName : slotName;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.clashywidgets.UPGRADE_ALARM");
        intent.putExtra(EXTRA_TASK_ID,   task.id);
        intent.putExtra(EXTRA_TASK_NAME, displayName);
        intent.putExtra(EXTRA_SLOT_NAME, slotName);

        return PendingIntent.getBroadcast(
                context,
                task.id,   // unique per task
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * Builds a matching PendingIntent for cancellation (same request code, no extras needed).
     */
    private static PendingIntent buildCancelIntent(Context context, int taskId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.clashywidgets.UPGRADE_ALARM");
        return PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
