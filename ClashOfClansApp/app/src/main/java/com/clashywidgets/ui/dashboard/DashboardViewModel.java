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

    private final MediatorLiveData<List<SlotWithTask>> filteredHomeBuilders = new MediatorLiveData<>();
    private final MediatorLiveData<List<SlotWithTask>> filteredLabSlots = new MediatorLiveData<>();

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

        setupFilters();
    }

    private void setupFilters() {
        // Filter Home Builders
        filteredHomeBuilders.addSource(rawHomeBuilders, list -> updateHomeBuilders());
        filteredHomeBuilders.addSource(numBuildersSetting, val -> updateHomeBuilders());
        filteredHomeBuilders.addSource(workForHireSetting, val -> updateHomeBuilders());

        // Filter Lab Slots
        filteredLabSlots.addSource(rawLabSlots, list -> updateLabSlots());
        filteredLabSlots.addSource(workForHireSetting, val -> updateLabSlots());


    }

    private void updateHomeBuilders() {
        List<SlotWithTask> builders = rawHomeBuilders.getValue();
        if (builders == null) return;

        String numStr = numBuildersSetting.getValue();
        int baseBuilders = (numStr != null) ? Integer.parseInt(numStr) : 6;
        
        boolean workForHire = "true".equals(workForHireSetting.getValue());
        int totalAllowed = workForHire ? baseBuilders + 1 : baseBuilders;

        List<SlotWithTask> filtered = new ArrayList<>();
        for (int i = 0; i < builders.size() && i < totalAllowed; i++) {
            filtered.add(builders.get(i));
        }
        filteredHomeBuilders.setValue(filtered);
    }

    private void updateLabSlots() {
        List<SlotWithTask> labs = rawLabSlots.getValue();
        if (labs == null) return;

        boolean workForHire = "true".equals(workForHireSetting.getValue());
        int totalAllowed = workForHire ? 2 : 1;

        List<SlotWithTask> filtered = new ArrayList<>();
        for (int i = 0; i < labs.size() && i < totalAllowed; i++) {
            filtered.add(labs.get(i));
        }
        filteredLabSlots.setValue(filtered);
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
        return builderBaseSlots;
    }

    public LiveData<List<SlotWithTask>> getStarLabSlots() {
        return starLabSlots;
    }
}
