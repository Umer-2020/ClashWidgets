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
            UpgradeTask task = upgradeTaskDao.getTaskSync(taskId);
            if (task != null) {
                task.endTime -= hoursToSubtract * 3600000L;
                upgradeTaskDao.update(task);
            }
        });
    }

    // --- Settings ---

    public LiveData<String> getSetting(String key) {
        return appSettingsDao.getSettingLiveData(key);
    }

    public String getSettingSync(String key) {
        return appSettingsDao.getSettingSync(key);
    }

    public void saveSetting(String key, String value) {
        executor.execute(() -> appSettingsDao.insertOrUpdate(new AppSettings(key, value)));
    }

    public void initializeDefaultSlotsIfNeeded() {
        executor.execute(() -> {
            java.util.List<WorkerSlot> existingSlots = workerSlotDao.getAllSlotsSync();
            java.util.List<WorkerSlot> slotsToAdd = new java.util.ArrayList<>();

            long homeBuilders = existingSlots.stream().filter(s -> "HOME".equals(s.village) && "BUILDER".equals(s.type)).count();
            for (long i = homeBuilders + 1; i <= 7; i++) {
                WorkerSlot slot = new WorkerSlot();
                slot.name = (i == 7) ? "Goblin Builder (Event)" : "Home Builder " + i;
                slot.village = "HOME";
                slot.type = "BUILDER";
                slotsToAdd.add(slot);
            }

            long labCount = existingSlots.stream().filter(s -> "HOME".equals(s.village) && "LAB".equals(s.type)).count();
            if (labCount < 1) {
                WorkerSlot lab1 = new WorkerSlot();
                lab1.name = "Laboratory";
                lab1.village = "HOME";
                lab1.type = "LAB";
                slotsToAdd.add(lab1);
            }
            if (labCount < 2) {
                WorkerSlot lab2 = new WorkerSlot();
                lab2.name = "Goblin Researcher (Event)";
                lab2.village = "HOME";
                lab2.type = "LAB";
                slotsToAdd.add(lab2);
            }

            boolean hasPet = existingSlots.stream().anyMatch(s -> "HOME".equals(s.village) && "PET".equals(s.type));
            if (!hasPet) {
                WorkerSlot pet = new WorkerSlot();
                pet.name = "Pet House";
                pet.village = "HOME";
                pet.type = "PET";
                slotsToAdd.add(pet);
            }

            long bbBuilders = existingSlots.stream().filter(s -> "BUILDER_BASE".equals(s.village) && "BUILDER".equals(s.type)).count();
            
            // Rename any existing builder base builders to generic names
            long bbIndex = 1;
            for (WorkerSlot s : existingSlots) {
                if ("BUILDER_BASE".equals(s.village) && "BUILDER".equals(s.type)) {
                    String genericName = "Builder Base Builder " + bbIndex++;
                    if (!genericName.equals(s.name)) {
                        s.name = genericName;
                        workerSlotDao.update(s);
                    }
                }
            }

            // Add any missing builder base builders up to 2
            for (long i = bbBuilders + 1; i <= 2; i++) {
                WorkerSlot slot = new WorkerSlot();
                slot.name = "Builder Base Builder " + i;
                slot.village = "BUILDER_BASE";
                slot.type = "BUILDER";
                slotsToAdd.add(slot);
            }

            boolean hasStarLab = existingSlots.stream().anyMatch(s -> "BUILDER_BASE".equals(s.village) && "LAB".equals(s.type));
            if (!hasStarLab) {
                WorkerSlot starLab = new WorkerSlot();
                starLab.name = "Star Laboratory";
                starLab.village = "BUILDER_BASE";
                starLab.type = "LAB";
                slotsToAdd.add(starLab);
            }

            if (!slotsToAdd.isEmpty()) {
                workerSlotDao.insertAll(slotsToAdd);
            }
        });
    }
}
