package com.clashywidgets.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import com.clashywidgets.R;
import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.repository.UpgradeRepository;
import com.clashywidgets.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Widget #2 – Detailed (4×4 / 5×4)
 *
 * Shows each individual slot with:
 *   • a colour-coded dot  (green = available, red = busy)
 *   • the building name being upgraded (or the slot name when idle)
 *   • a live Chronometer counting DOWN to zero  (busy slots only)
 *   • the exact finish date/time in 12-hour AM/PM format  (busy slots only)
 *
 * Chronometer trick for count-DOWN in RemoteViews:
 *   base = SystemClock.elapsedRealtime() - (endTime - System.currentTimeMillis())
 *   The Chronometer then counts upward from a negative offset, which visually
 *   appears as a count-down. Android's Chronometer doesn't natively count down
 *   in RemoteViews (setCountDown() is not a RemoteViews-safe method), so we use
 *   this well-known workaround — the format shows negative numbers once it passes
 *   zero, but for upgrades that's rare and acceptable.
 *
 * Update strategy: same as MinimalWidgetProvider — UpgradeRepository calls
 *   requestUpdate() after every DB write.
 */
public class DetailedWidgetProvider extends AppWidgetProvider {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat FINISH_FMT =
            new SimpleDateFormat("h:mm a, MMM d", Locale.getDefault());

    // ── Entry point ──────────────────────────────────────────────────────────

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    // ── Core update logic ────────────────────────────────────────────────────

    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        executor.execute(() -> {
            UpgradeRepository repo = UpgradeRepository.getInstance(context);

            List<SlotWithTask> homeBuilders = repo.getHomeBuildersWithTasksSync();
            List<SlotWithTask> homeLabs     = repo.getLabSlotsWithTasksSync();
            List<SlotWithTask> homePets     = repo.getPetSlotsWithTasksSync();
            List<SlotWithTask> bbBuilders   = repo.getBuilderBaseSlotsWithTasksSync();
            List<SlotWithTask> starLabs     = repo.getStarLabSlotsWithTasksSync();

            boolean goblinEvent = "true".equals(repo.getSettingSync("goblin_event_active"));
            int numBuilders     = parseIntSafe(repo.getSettingSync("num_home_builders"), 6);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_detailed);

            // Last-updated timestamp
            views.setTextViewText(R.id.widget_detailed_updated,
                    new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date()));

            // ── Home Builders (slots 1-6, slot 7 = Goblin) ──────────────────
            int[][] builderSlotIds = {
                {R.id.widget_detailed_hb1_dot, R.id.widget_detailed_hb1_name,
                 R.id.widget_detailed_hb1_timer, R.id.widget_detailed_hb1_finish},
                {R.id.widget_detailed_hb2_dot, R.id.widget_detailed_hb2_name,
                 R.id.widget_detailed_hb2_timer, R.id.widget_detailed_hb2_finish},
                {R.id.widget_detailed_hb3_dot, R.id.widget_detailed_hb3_name,
                 R.id.widget_detailed_hb3_timer, R.id.widget_detailed_hb3_finish},
                {R.id.widget_detailed_hb4_dot, R.id.widget_detailed_hb4_name,
                 R.id.widget_detailed_hb4_timer, R.id.widget_detailed_hb4_finish},
                {R.id.widget_detailed_hb5_dot, R.id.widget_detailed_hb5_name,
                 R.id.widget_detailed_hb5_timer, R.id.widget_detailed_hb5_finish},
                {R.id.widget_detailed_hb6_dot, R.id.widget_detailed_hb6_name,
                 R.id.widget_detailed_hb6_timer, R.id.widget_detailed_hb6_finish},
            };

            // Bind slots 1–6 (up to numBuilders visible)
            for (int i = 0; i < 6; i++) {
                if (i < numBuilders && i < homeBuilders.size()) {
                    bindSlotRow(views, homeBuilders.get(i), builderSlotIds[i]);
                } else {
                    // Slot beyond user's builder count — hide it
                    setSlotHidden(views, builderSlotIds[i]);
                }
            }

            // Goblin Builder row (slot 7)
            if (goblinEvent && homeBuilders.size() >= 7) {
                views.setViewVisibility(R.id.widget_detailed_goblin_builder_row, View.VISIBLE);
                bindSlotRow(views, homeBuilders.get(6),
                        new int[]{R.id.widget_detailed_hb7_dot, R.id.widget_detailed_hb7_name,
                                  R.id.widget_detailed_hb7_timer, R.id.widget_detailed_hb7_finish});
            } else {
                views.setViewVisibility(R.id.widget_detailed_goblin_builder_row, View.GONE);
            }

            // ── Lab (slot 1) ─────────────────────────────────────────────────
            if (!homeLabs.isEmpty()) {
                bindLabRow(views, homeLabs.get(0),
                        R.id.widget_detailed_lab_dot,
                        R.id.widget_detailed_lab_timer,
                        R.id.widget_detailed_lab_status);
            }

            // Goblin Researcher row (slot 2)
            if (goblinEvent && homeLabs.size() >= 2) {
                views.setViewVisibility(R.id.widget_detailed_goblin_lab_row, View.VISIBLE);
                bindLabRow(views, homeLabs.get(1),
                        R.id.widget_detailed_lab2_dot,
                        R.id.widget_detailed_lab2_timer,
                        R.id.widget_detailed_lab2_status);
            } else {
                views.setViewVisibility(R.id.widget_detailed_goblin_lab_row, View.GONE);
            }

            // ── Pet House ────────────────────────────────────────────────────
            if (!homePets.isEmpty()) {
                bindLabRow(views, homePets.get(0),
                        R.id.widget_detailed_pet_dot,
                        R.id.widget_detailed_pet_timer,
                        R.id.widget_detailed_pet_status);
            }

            // ── Builder Base ─────────────────────────────────────────────────
            int[][] bbSlotIds = {
                {R.id.widget_detailed_bb1_dot, R.id.widget_detailed_bb1_name,
                 R.id.widget_detailed_bb1_timer, R.id.widget_detailed_bb1_finish},
                {R.id.widget_detailed_bb2_dot, R.id.widget_detailed_bb2_name,
                 R.id.widget_detailed_bb2_timer, R.id.widget_detailed_bb2_finish},
            };
            for (int i = 0; i < 2; i++) {
                if (i < bbBuilders.size()) {
                    bindSlotRow(views, bbBuilders.get(i), bbSlotIds[i]);
                } else {
                    setSlotHidden(views, bbSlotIds[i]);
                }
            }

            // ── Star Lab ─────────────────────────────────────────────────────
            if (!starLabs.isEmpty()) {
                bindLabRow(views, starLabs.get(0),
                        R.id.widget_detailed_starlab_dot,
                        R.id.widget_detailed_starlab_timer,
                        R.id.widget_detailed_starlab_status);
            }

            // ── Tap → open app ───────────────────────────────────────────────
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_detailed_root, pendingIntent);

            manager.updateAppWidget(widgetId, views);
        });
    }

    // ── Slot binding helpers ─────────────────────────────────────────────────

    /**
     * Binds a builder/worker slot row:
     * ids[0]=dot  ids[1]=name  ids[2]=chronometer  ids[3]=finishText
     */
    private static void bindSlotRow(RemoteViews views, SlotWithTask swt, int[] ids) {
        int dotId        = ids[0];
        int nameId       = ids[1];
        int chronoId     = ids[2];
        int finishTextId = ids[3];

        if (swt.task != null) {
            // Busy
            views.setInt(dotId, "setBackgroundColor", 0xFFF44336);  // red

            String label = (swt.task.buildingName != null && !swt.task.buildingName.isEmpty())
                    ? swt.task.buildingName : swt.slot.name;
            views.setTextViewText(nameId, label);

            long millisLeft = swt.task.endTime - System.currentTimeMillis();
            if (millisLeft <= 0) {
                // Time has already expired
                views.setViewVisibility(chronoId, View.GONE);
                views.setTextViewText(finishTextId, "Finished");
                views.setTextColor(finishTextId, 0xFF4CAF50); // green
                views.setViewVisibility(finishTextId, View.VISIBLE);
            } else {
                // Natively count down using setCountDown reflection (API 24+)
                long chronometerBase = SystemClock.elapsedRealtime() + millisLeft;
                views.setChronometer(chronoId, chronometerBase, null, true);
                views.setBoolean(chronoId, "setCountDown", true);
                views.setViewVisibility(chronoId, View.VISIBLE);

                views.setTextViewText(finishTextId, FINISH_FMT.format(new Date(swt.task.endTime)));
                views.setTextColor(finishTextId, 0xFFFFD04B);           // gold finish time
                views.setViewVisibility(finishTextId, View.GONE);        // show chrono only
            }
        } else {
            // Available
            views.setInt(dotId, "setBackgroundColor", 0xFF4CAF50);  // green
            views.setTextViewText(nameId, swt.slot.name);
            views.setViewVisibility(chronoId, View.GONE);
            views.setTextViewText(finishTextId, "Available");
            views.setTextColor(finishTextId, 0xFF4CAF50);
            views.setViewVisibility(finishTextId, View.VISIBLE);
        }
    }

    /**
     * Binds a single-indicator row (Lab / Pet / Star Lab) that only has a dot,
     * chronometer, and status text (no name label).
     */
    private static void bindLabRow(RemoteViews views, SlotWithTask swt,
                                   int dotId, int chronoId, int statusId) {
        if (swt.task != null) {
            views.setInt(dotId, "setBackgroundColor", 0xFFF44336);
            long millisLeft = swt.task.endTime - System.currentTimeMillis();
            if (millisLeft <= 0) {
                views.setViewVisibility(chronoId, View.GONE);
                views.setTextViewText(statusId, "Finished");
                views.setTextColor(statusId, 0xFF4CAF50);
                views.setViewVisibility(statusId, View.VISIBLE);
            } else {
                long base = SystemClock.elapsedRealtime() + millisLeft;
                views.setChronometer(chronoId, base, null, true);
                views.setBoolean(chronoId, "setCountDown", true);
                views.setViewVisibility(chronoId, View.VISIBLE);
                views.setViewVisibility(statusId, View.GONE);
            }
        } else {
            views.setInt(dotId, "setBackgroundColor", 0xFF4CAF50);
            views.setViewVisibility(chronoId, View.GONE);
            views.setTextViewText(statusId, "Available");
            views.setTextColor(statusId, 0xFF4CAF50);
            views.setViewVisibility(statusId, View.VISIBLE);
        }
    }

    /** Hides all views of a slot row (used for slots beyond user's builder count). */
    private static void setSlotHidden(RemoteViews views, int[] ids) {
        for (int id : ids) views.setViewVisibility(id, View.GONE);
    }

    // ── Static refresh trigger ───────────────────────────────────────────────

    /** Called from UpgradeRepository to refresh all placed instances. */
    public static void requestUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component  = new ComponentName(context, DetailedWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    // ── Utility ──────────────────────────────────────────────────────────────

    private static int parseIntSafe(String value, int defaultVal) {
        if (value == null) return defaultVal;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}
