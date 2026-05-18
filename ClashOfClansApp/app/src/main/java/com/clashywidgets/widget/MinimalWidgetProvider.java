package com.clashywidgets.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.clashywidgets.R;
import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.db.WorkerSlot;
import com.clashywidgets.data.repository.UpgradeRepository;
import com.clashywidgets.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Widget #1 – Minimal (2×3 / 2×4)
 *
 * Displays high-level slot availability counts for both villages at a glance.
 * e.g.  👷 Builders  3/6    🔬 Lab  0/1    🐾 Pet  0/1
 *
 * Update strategy:
 *   - onUpdate() is called when the widget is first added or the system decides to refresh it.
 *   - All data-driven updates are triggered by UpgradeRepository.broadcastWidgetUpdate(), which
 *     sends ACTION_APPWIDGET_UPDATE to this provider whenever the DB changes (task added/deleted/
 *     boosted).  This keeps the widget in sync without requiring periodic polling.
 */
public class MinimalWidgetProvider extends AppWidgetProvider {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── Entry points ─────────────────────────────────────────────────────────

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    // ── Core update logic ────────────────────────────────────────────────────

    /**
     * Queries the database on a background thread and pushes fresh RemoteViews
     * to the given widget instance.
     */
    static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        executor.execute(() -> {
            UpgradeRepository repo = UpgradeRepository.getInstance(context);

            // Read slot lists synchronously (safe on background thread)
            List<SlotWithTask> homeBuilders  = repo.getHomeBuildersWithTasksSync();
            List<SlotWithTask> homeLabs      = repo.getLabSlotsWithTasksSync();
            List<SlotWithTask> homePets      = repo.getPetSlotsWithTasksSync();
            List<SlotWithTask> bbBuilders    = repo.getBuilderBaseSlotsWithTasksSync();
            List<SlotWithTask> starLabs      = repo.getStarLabSlotsWithTasksSync();

            // Read goblin event setting
            boolean goblinEvent = "true".equals(repo.getSettingSync("goblin_event_active"));
            int numBuilders     = parseIntSafe(repo.getSettingSync("num_home_builders"), 6);

            // Build counts
            // Active = slot has a task; available = no task
            int totalBuilders = goblinEvent ? Math.min(numBuilders + 1, 7) : numBuilders;
            int availBuilders = countAvailable(homeBuilders, totalBuilders);

            int totalLabs = goblinEvent ? 2 : 1;
            int availLabs = countAvailable(homeLabs, totalLabs);

            int availPets = countAvailable(homePets, 1);

            int totalBBBuilders = bbBuilders.size();
            int availBBBuilders = countAvailable(bbBuilders, totalBBBuilders);

            int availStarLab = countAvailable(starLabs, 1);

            // Build RemoteViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_minimal);

            // Set counts
            views.setTextViewText(R.id.widget_minimal_home_builders,
                    availBuilders + "/" + totalBuilders);
            views.setTextViewText(R.id.widget_minimal_home_lab,
                    availLabs + "/" + totalLabs);
            views.setTextViewText(R.id.widget_minimal_home_pet,
                    availPets + "/1");
            views.setTextViewText(R.id.widget_minimal_bb_builders,
                    availBBBuilders + "/" + Math.max(totalBBBuilders, 1));
            views.setTextViewText(R.id.widget_minimal_bb_lab,
                    availStarLab + "/1");

            // Color-code builder count: green = some available, red = all busy
            int builderColor = availBuilders > 0 ? 0xFF4CAF50 : 0xFFF44336;
            views.setTextColor(R.id.widget_minimal_home_builders, builderColor);

            int bbBuilderColor = availBBBuilders > 0 ? 0xFF4CAF50 : 0xFFF44336;
            views.setTextColor(R.id.widget_minimal_bb_builders, bbBuilderColor);

            // Tap → open app
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_minimal_root, pendingIntent);

            manager.updateAppWidget(widgetId, views);
        });
    }

    // ── Helper to trigger all instances of this widget ───────────────────────

    /** Called from UpgradeRepository to refresh all placed instances. */
    public static void requestUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component  = new ComponentName(context, MinimalWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    /**
     * Counts how many of the first {@code limit} slots have no active task.
     */
    private static int countAvailable(List<SlotWithTask> slots, int limit) {
        int available = 0;
        int count = Math.min(slots.size(), limit);
        for (int i = 0; i < count; i++) {
            if (slots.get(i).task == null) available++;
        }
        return available;
    }

    private static int parseIntSafe(String value, int defaultVal) {
        if (value == null) return defaultVal;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}
