package com.clashywidgets.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.repository.UpgradeRepository;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final UpgradeRepository repository;

    private final LiveData<List<SlotWithTask>> rawHomeBuilders;
    private final LiveData<List<SlotWithTask>> rawLabSlots;
    private final LiveData<List<SlotWithTask>> petSlots;
    private final LiveData<List<SlotWithTask>> builderBaseSlots;
    private final LiveData<List<SlotWithTask>> starLabSlots;

    private final LiveData<String> numBuildersSetting;
    private final LiveData<String> workForHireSetting;
    private final LiveData<String> numBbBuildersSetting;

    private final MediatorLiveData<List<SlotWithTask>> filteredHomeBuilders = new MediatorLiveData<>();
    private final MediatorLiveData<List<SlotWithTask>> filteredLabSlots = new MediatorLiveData<>();
    private final MediatorLiveData<List<SlotWithTask>> filteredBuilderBaseSlots = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = UpgradeRepository.getInstance(application);

        rawHomeBuilders = repository.getHomeBuildersWithTasks();
        rawLabSlots = repository.getLabSlotsWithTasks();
        petSlots = repository.getPetSlotsWithTasks();
        builderBaseSlots = repository.getBuilderBaseSlotsWithTasks();
        starLabSlots = repository.getStarLabSlotsWithTasks();

        numBuildersSetting = repository.getSetting("num_builders");
        workForHireSetting = repository.getSetting("work_for_hire");
        numBbBuildersSetting = repository.getSetting("num_bb_builders");

        setupFilters();
    }

    private void setupFilters() {
        // Filter Home Builders (affected by num_builders + work_for_hire)
        filteredHomeBuilders.addSource(rawHomeBuilders, list -> updateHomeBuilders());
        filteredHomeBuilders.addSource(numBuildersSetting, val -> updateHomeBuilders());
        filteredHomeBuilders.addSource(workForHireSetting, val -> updateHomeBuilders());

        // Filter Lab Slots (affected by work_for_hire only)
        filteredLabSlots.addSource(rawLabSlots, list -> updateLabSlots());
        filteredLabSlots.addSource(workForHireSetting, val -> updateLabSlots());

        // Filter Builder Base Slots (affected by num_bb_builders)
        filteredBuilderBaseSlots.addSource(builderBaseSlots, list -> updateBuilderBaseSlots());
        filteredBuilderBaseSlots.addSource(numBbBuildersSetting, val -> updateBuilderBaseSlots());
    }

    private void updateHomeBuilders() {
        List<SlotWithTask> builders = rawHomeBuilders.getValue();
        if (builders == null) return;

        String numStr = numBuildersSetting.getValue();
        // Default to 5 builders if not set (most players have 5)
        int baseBuilders = (numStr != null) ? Integer.parseInt(numStr) : 5;
        
        boolean workForHire = "true".equals(workForHireSetting.getValue());

        List<SlotWithTask> filtered = new ArrayList<>();

        // First, add all normal builders up to baseBuilders exactly by name
        for (int i = 1; i <= baseBuilders; i++) {
            String nameToFind = "Home Builder " + i;
            for (SlotWithTask slot : builders) {
                if (nameToFind.equals(slot.slot.name)) {
                    filtered.add(slot);
                    break;
                }
            }
        }

        // Then, add the Goblin Builder if the event is active
        if (workForHire) {
            for (SlotWithTask slot : builders) {
                if (slot.slot.name.contains("Goblin")) {
                    filtered.add(slot);
                }
            }
        }
        filteredHomeBuilders.setValue(filtered);
    }

    private void updateLabSlots() {
        List<SlotWithTask> labs = rawLabSlots.getValue();
        if (labs == null) return;

        boolean workForHire = "true".equals(workForHireSetting.getValue());

        List<SlotWithTask> filtered = new ArrayList<>();
        
        // Add normal laboratory
        for (SlotWithTask slot : labs) {
            if (!slot.slot.name.contains("Goblin")) {
                filtered.add(slot);
            }
        }

        // Add Goblin Researcher if the event is active
        if (workForHire) {
            for (SlotWithTask slot : labs) {
                if (slot.slot.name.contains("Goblin")) {
                    filtered.add(slot);
                }
            }
        }
        filteredLabSlots.setValue(filtered);
    }

    private void updateBuilderBaseSlots() {
        List<SlotWithTask> bbSlots = builderBaseSlots.getValue();
        if (bbSlots == null) return;

        String numBbStr = numBbBuildersSetting.getValue();
        int baseBbBuilders = (numBbStr != null) ? Integer.parseInt(numBbStr) : 1;

        List<SlotWithTask> filtered = new ArrayList<>();
        
        // Exact match for Builder Base Builders
        for (int i = 1; i <= baseBbBuilders; i++) {
            String nameToFind = "Builder Base Builder " + i;
            for (SlotWithTask slot : bbSlots) {
                if (nameToFind.equals(slot.slot.name)) {
                    filtered.add(slot);
                    break;
                }
            }
        }
        filteredBuilderBaseSlots.setValue(filtered);
    }


    public LiveData<List<SlotWithTask>> getHomeBuilders() {
        return filteredHomeBuilders;
    }

    public LiveData<List<SlotWithTask>> getLabSlots() {
        return filteredLabSlots;
    }

    public LiveData<List<SlotWithTask>> getPetSlots() {
        return petSlots;
    }

    public LiveData<List<SlotWithTask>> getBuilderBaseSlots() {
        return filteredBuilderBaseSlots;
    }

    public LiveData<List<SlotWithTask>> getStarLabSlots() {
        return starLabSlots;
    }
}
