package com.clashywidgets.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.repository.UpgradeRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final UpgradeRepository repository;

    private final LiveData<List<SlotWithTask>> homeBuilders;
    private final LiveData<List<SlotWithTask>> labSlots;
    private final LiveData<List<SlotWithTask>> petSlots;
    private final LiveData<List<SlotWithTask>> builderBaseSlots;
    private final LiveData<List<SlotWithTask>> starLabSlots;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = UpgradeRepository.getInstance(application);

        homeBuilders = repository.getHomeBuildersWithTasks();
        labSlots = repository.getLabSlotsWithTasks();
        petSlots = repository.getPetSlotsWithTasks();
        builderBaseSlots = repository.getBuilderBaseSlotsWithTasks();
        starLabSlots = repository.getStarLabSlotsWithTasks();
    }

    public LiveData<List<SlotWithTask>> getHomeBuilders() {
        return homeBuilders;
    }

    public LiveData<List<SlotWithTask>> getLabSlots() {
        return labSlots;
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
