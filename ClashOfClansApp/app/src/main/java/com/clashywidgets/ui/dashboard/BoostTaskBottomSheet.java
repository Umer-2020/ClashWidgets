package com.clashywidgets.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clashywidgets.R;
import com.clashywidgets.data.db.SlotWithTask;
import com.clashywidgets.data.db.UpgradeTask;
import com.clashywidgets.data.db.WorkerSlot;
import com.clashywidgets.data.repository.UpgradeRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.content.Context;
import android.content.SharedPreferences;

public class BoostTaskBottomSheet extends BottomSheetDialogFragment {

    private final SlotWithTask slotWithTask;

    public BoostTaskBottomSheet(SlotWithTask slotWithTask) {
        this.slotWithTask = slotWithTask;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_boost_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UpgradeTask task = slotWithTask.task;
        WorkerSlot slot = slotWithTask.slot;

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(task.buildingName);

        TextView tvBoostDesc = view.findViewById(R.id.tv_boost_desc);
        boolean isLab = "LAB".equals(slot.type);
        if (isLab) {
            tvBoostDesc.setText("Apply Lab Assistant Boost (Subtracts Hours)");
        } else {
            tvBoostDesc.setText("Apply Builder's Apprentice Boost (Subtracts Hours)");
        }

        NumberPicker npBoostHours = view.findViewById(R.id.np_boost_hours);
        npBoostHours.setMinValue(1);
        npBoostHours.setMaxValue(12);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String prefKey = isLab ? "default_lab_assistant_boost" : "default_builders_apprentice_boost";
        int defaultHours = prefs.getInt(prefKey, isLab ? 1 : 8);

        // Ensure default is within bounds
        if (defaultHours > npBoostHours.getMaxValue()) defaultHours = npBoostHours.getMaxValue();
        if (defaultHours < npBoostHours.getMinValue()) defaultHours = npBoostHours.getMinValue();
        
        npBoostHours.setValue(defaultHours);

        view.findViewById(R.id.btn_apply_boost).setOnClickListener(v -> {
            int hours = npBoostHours.getValue();
            prefs.edit().putInt(prefKey, hours).apply(); // Save the new default

            long millisToSubtract = hours * 60L * 60L * 1000L;
            
            task.endTime -= millisToSubtract;
            UpgradeRepository.getInstance(getContext()).updateTask(task);
            
            Toast.makeText(getContext(), "Boost applied!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            UpgradeRepository.getInstance(getContext()).deleteTask(task.id);
            Toast.makeText(getContext(), "Upgrade removed", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}
