package com.clashywidgets.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.clashywidgets.data.db.AppDatabase;
import com.clashywidgets.data.db.AppSettings;
import com.clashywidgets.data.db.AppSettingsDao;
import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.db.UpgradeTask;
import com.clashywidgets.data.db.UpgradeTaskDao;
import com.clashywidgets.data.db.WorkerSlot;
import com.clashywidgets.data.db.WorkerSlotDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository – the single source of truth for all upgrade and settings data.
 *
 * The UI layer (ViewModels) should ONLY interact with data through this class,
 * never directly with DAOs or the database.
 */
public class UpgradeRepository {

    private static volatile UpgradeRepository INSTANCE;
    private final AppDatabase db;
    private final WorkerSlotDao workerSlotDao;
    private final UpgradeTaskDao upgradeTaskDao;
    private final AppSettingsDao appSettingsDao;
    private final ExecutorService executor;

    private UpgradeRepository(Context context) {
        db = AppDatabase.getInstance(context);
        workerSlotDao = db.workerSlotDao();
        upgradeTaskDao = db.upgradeTaskDao();
        appSettingsDao = db.appSettingsDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public static UpgradeRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UpgradeRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UpgradeRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // --- Queries ---

    public LiveData<List<WorkerSlot>> getHomeBuilders() {
        return workerSlotDao.getSlots("HOME", "BUILDER");
    }

    public LiveData<List<WorkerSlot>> getLabSlots() {
        return workerSlotDao.getSlots("HOME", "LAB");
    }

    public LiveData<List<WorkerSlot>> getPetSlots() {
        return workerSlotDao.getSlots("HOME", "PET");
    }

    public LiveData<List<WorkerSlot>> getBuilderBaseSlots() {
        return workerSlotDao.getSlots("BUILDER_BASE", "BUILDER");
    }
    
    public LiveData<List<WorkerSlot>> getStarLabSlots() {
        return workerSlotDao.getSlots("BUILDER_BASE", "LAB");
    }

    public LiveData<List<SlotWithTask>> getHomeBuildersWithTasks() {
        return workerSlotDao.getSlotsWithTasks("HOME", "BUILDER");
    }

    public LiveData<List<SlotWithTask>> getLabSlotsWithTasks() {
        return workerSlotDao.getSlotsWithTasks("HOME", "LAB");
    }

    public LiveData<List<SlotWithTask>> getPetSlotsWithTasks() {
        return workerSlotDao.getSlotsWithTasks("HOME", "PET");
    }

    public LiveData<List<SlotWithTask>> getBuilderBaseSlotsWithTasks() {
        return workerSlotDao.getSlotsWithTasks("BUILDER_BASE", "BUILDER");
    }
    
    public LiveData<List<SlotWithTask>> getStarLabSlotsWithTasks() {
        return workerSlotDao.getSlotsWithTasks("BUILDER_BASE", "LAB");
    }

    public LiveData<List<UpgradeTask>> getAllTasks() {
        return upgradeTaskDao.getAllTasks();
    }
    
    public LiveData<UpgradeTask> getTaskForSlot(int slotId) {
        return upgradeTaskDao.getTaskForSlot(slotId);
    }

    // --- Writes ---

    public void insertTask(UpgradeTask task) {
        executor.execute(() -> upgradeTaskDao.insert(task));
    }

    public void deleteTask(int taskId) {
        executor.execute(() -> upgradeTaskDao.deleteTask(taskId));
    }

    public void updateTask(UpgradeTask task) {
        executor.execute(() -> upgradeTaskDao.update(task));
    }

    // Phase 3: Helper boost
    public void applyHelperBoost(int taskId, int hoursToSubtract) {
        executor.execute(() -> {
            // Implementation pending: fetch task synchronously, update time, save
        });
    }

    // --- Settings ---

    public LiveData<String> getSetting(String key) {
        return appSettingsDao.getSettingLiveData(key);
    }

    public void saveSetting(String key, String value) {
        executor.execute(() -> appSettingsDao.insertOrUpdate(new AppSettings(key, value)));
    }

    public void initializeDefaultSlotsIfNeeded() {
        executor.execute(() -> {
            if (workerSlotDao.getAllSlotsSync().isEmpty()) {
                java.util.List<WorkerSlot> slots = new java.util.ArrayList<>();
                for (int i = 1; i <= 6; i++) {
                    WorkerSlot slot = new WorkerSlot();
                    slot.name = "Home Builder " + i;
                    slot.village = "HOME";
                    slot.type = "BUILDER";
                    slots.add(slot);
                }
                
                WorkerSlot lab = new WorkerSlot();
                lab.name = "Laboratory";
                lab.village = "HOME";
                lab.type = "LAB";
                slots.add(lab);

                WorkerSlot pet = new WorkerSlot();
                pet.name = "Pet House";
                pet.village = "HOME";
                pet.type = "PET";
                slots.add(pet);

                WorkerSlot bb1 = new WorkerSlot();
                bb1.name = "Master Builder";
                bb1.village = "BUILDER_BASE";
                bb1.type = "BUILDER";
                slots.add(bb1);
                
                WorkerSlot bb2 = new WorkerSlot();
                bb2.name = "B.O.B";
                bb2.village = "BUILDER_BASE";
                bb2.type = "BUILDER";
                slots.add(bb2);

                WorkerSlot starLab = new WorkerSlot();
                starLab.name = "Star Laboratory";
                starLab.village = "BUILDER_BASE";
                starLab.type = "LAB";
                slots.add(starLab);

                workerSlotDao.insertAll(slots);
            }
        });
    }
}
