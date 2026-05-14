package com.clashywidgets.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clashywidgets.databinding.FragmentSettingsBinding;
import com.clashywidgets.data.repository.UpgradeRepository;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        UpgradeRepository repo = UpgradeRepository.getInstance(requireContext());

        // Initialize NumberPickers
        binding.npNumBuilders.setMinValue(1);
        binding.npNumBuilders.setMaxValue(6);

        binding.npApprenticeLevel.setMinValue(1);
        binding.npApprenticeLevel.setMaxValue(8);

        binding.npLabAssistantLevel.setMinValue(1);
        binding.npLabAssistantLevel.setMaxValue(12);

        // Load Settings
        repo.getSetting("num_builders").observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                binding.npNumBuilders.setValue(Integer.parseInt(value));
            } else {
                binding.npNumBuilders.setValue(6); // Default 6 builders
            }
        });

        repo.getSetting("apprentice_level").observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                binding.npApprenticeLevel.setValue(Integer.parseInt(value));
            } else {
                binding.npApprenticeLevel.setValue(8); // Default level 8
            }
        });

        repo.getSetting("lab_assistant_level").observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                binding.npLabAssistantLevel.setValue(Integer.parseInt(value));
            } else {
                binding.npLabAssistantLevel.setValue(1); // Default level 1
            }
        });

        repo.getSetting("work_for_hire").observe(getViewLifecycleOwner(), value -> {
            binding.switchWorkForHire.setChecked("true".equals(value));
        });

        // Set Listeners
        binding.npNumBuilders.setOnValueChangedListener((picker, oldVal, newVal) -> {
            repo.saveSetting("num_builders", String.valueOf(newVal));
        });

        binding.npApprenticeLevel.setOnValueChangedListener((picker, oldVal, newVal) -> {
            repo.saveSetting("apprentice_level", String.valueOf(newVal));
        });

        binding.npLabAssistantLevel.setOnValueChangedListener((picker, oldVal, newVal) -> {
            repo.saveSetting("lab_assistant_level", String.valueOf(newVal));
        });

        binding.switchWorkForHire.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repo.saveSetting("work_for_hire", isChecked ? "true" : "false");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
