package com.clashywidgets.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database singleton.
 *
 * Entities and DAOs are defined in Phase 2.
 * Add them to the @Database annotation's `entities` list as they are created.
 *
 * Example (Phase 2):
 *
 *   @Database(
 *       entities = {WorkerSlot.class, UpgradeTask.class, AppSettings.class},
 *       version  = 1,
 *       exportSchema = false
 *   )
 */
@Database(entities = {WorkerSlot.class, UpgradeTask.class, AppSettings.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAOs
    public abstract WorkerSlotDao workerSlotDao();
    public abstract UpgradeTaskDao upgradeTaskDao();
    public abstract AppSettingsDao appSettingsDao();

    /**
     * Returns the singleton database instance, creating it on first call.
     * Safe for multi-threaded access (double-checked locking + volatile).
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "clashywidgets.db"
                            )
                            // Wipe and rebuild on schema change during development.
                            // Replace with proper Migrations before production.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
