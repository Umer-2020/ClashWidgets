package com.clashywidgets.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clashywidgets.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Adapters
        WorkerSlotAdapter homeBuilderAdapter = new WorkerSlotAdapter(this::onSlotClicked);
        WorkerSlotAdapter labAdapter = new WorkerSlotAdapter(this::onSlotClicked);
        WorkerSlotAdapter petAdapter = new WorkerSlotAdapter(this::onSlotClicked);
        WorkerSlotAdapter bbAdapter = new WorkerSlotAdapter(this::onSlotClicked);
        WorkerSlotAdapter starLabAdapter = new WorkerSlotAdapter(this::onSlotClicked);

        // Setup RecyclerViews
        binding.rvHomeBuilders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHomeBuilders.setAdapter(homeBuilderAdapter);

        binding.rvLab.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLab.setAdapter(labAdapter);

        binding.rvPet.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPet.setAdapter(petAdapter);

        binding.rvBuilderBaseBuilders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBuilderBaseBuilders.setAdapter(bbAdapter);

        binding.rvStarLab.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvStarLab.setAdapter(starLabAdapter);

        // Observers
        viewModel.getHomeBuilders().observe(getViewLifecycleOwner(), homeBuilderAdapter::submitList);
        viewModel.getLabSlots().observe(getViewLifecycleOwner(), labAdapter::submitList);
        viewModel.getPetSlots().observe(getViewLifecycleOwner(), petAdapter::submitList);
        viewModel.getBuilderBaseSlots().observe(getViewLifecycleOwner(), bbAdapter::submitList);
        viewModel.getStarLabSlots().observe(getViewLifecycleOwner(), starLabAdapter::submitList);
    }

    private void onSlotClicked(com.clashywidgets.data.db.SlotWithTask slotWithTask) {
        if (slotWithTask.task == null) {
            AddTaskBottomSheet sheet = new AddTaskBottomSheet(slotWithTask.slot.id, slotWithTask.slot.name);
            sheet.show(getChildFragmentManager(), "AddTask");
        } else {
            BoostTaskBottomSheet sheet = new BoostTaskBottomSheet(slotWithTask.task);
            sheet.show(getChildFragmentManager(), "BoostTask");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
