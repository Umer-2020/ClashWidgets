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
import com.clashywidgets.data.db.UpgradeTask;
import com.clashywidgets.data.repository.UpgradeRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BoostTaskBottomSheet extends BottomSheetDialogFragment {

    private final UpgradeTask task;

    public BoostTaskBottomSheet(UpgradeTask task) {
        this.task = task;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_boost_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(task.buildingName);

        NumberPicker npBoostHours = view.findViewById(R.id.np_boost_hours);
        npBoostHours.setMinValue(1);
        npBoostHours.setMaxValue(24);
        npBoostHours.setValue(8); // Default 8x apprentice

        view.findViewById(R.id.btn_apply_boost).setOnClickListener(v -> {
            int hours = npBoostHours.getValue();
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
