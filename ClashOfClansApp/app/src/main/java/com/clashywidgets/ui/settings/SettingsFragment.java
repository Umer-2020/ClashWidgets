package com.clashywidgets.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.clashywidgets.databinding.FragmentSettingsBinding;

/**
 * Settings screen.
 * Will expose:
 *  - Builder's Apprentice level selector  (levels 1–8)
 *  - Lab Assistant level selector         (levels 1–12)
 *  - Goblin Event toggle (enables +1 Builder slot and +1 Lab slot in the UI & widgets)
 *  - Number-of-Builders selector          (5 or 6 – for players who haven't unlocked all builders)
 *  ---
 * Phase 3 implements full UI and persistence via AppSettings in Room.
 */
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
