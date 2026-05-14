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
import com.google.android.material.textfield.TextInputEditText;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private final int slotId;
    private final String slotName;

    public AddTaskBottomSheet(int slotId, String slotName) {
        this.slotId = slotId;
        this.slotName = slotName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText("Start Upgrade on " + slotName);

        TextInputEditText etBuilding = view.findViewById(R.id.et_building_name);
        NumberPicker npDays = view.findViewById(R.id.np_days);
        NumberPicker npHours = view.findViewById(R.id.np_hours);
        NumberPicker npMinutes = view.findViewById(R.id.np_minutes);

        npDays.setMinValue(0);
        npDays.setMaxValue(30);

        npHours.setMinValue(0);
        npHours.setMaxValue(23);

        npMinutes.setMinValue(0);
        npMinutes.setMaxValue(59);

        view.findViewById(R.id.btn_start).setOnClickListener(v -> {
            int d = npDays.getValue();
            int h = npHours.getValue();
            int m = npMinutes.getValue();
            
            long durationMillis = (d * 24L * 60L * 60L * 1000L) +
                                  (h * 60L * 60L * 1000L) +
                                  (m * 60L * 1000L);

            if (durationMillis <= 0) {
                Toast.makeText(getContext(), "Duration must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            String buildingName = etBuilding.getText() != null ? etBuilding.getText().toString() : "";
            if (buildingName.trim().isEmpty()) {
                buildingName = "Unknown Upgrade";
            }

            UpgradeTask task = new UpgradeTask();
            task.slotId = slotId;
            task.buildingName = buildingName;
            task.endTime = System.currentTimeMillis() + durationMillis;

            UpgradeRepository.getInstance(getContext()).insertTask(task);

            dismiss();
        });
    }
}
