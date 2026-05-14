package com.clashywidgets.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.clashywidgets.R;
import com.clashywidgets.data.db.SlotWithTask;

import java.util.ArrayList;
import java.util.List;

public class WorkerSlotAdapter extends RecyclerView.Adapter<WorkerSlotAdapter.SlotViewHolder> {

    private final List<SlotWithTask> items = new ArrayList<>();
    private final OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(SlotWithTask slotWithTask);
    }

    public WorkerSlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SlotWithTask> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_worker_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSlotName;
        private final TextView tvStatus;
        private final TextView tvBuildingName;
        private final TextView tvCountdown;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlotName = itemView.findViewById(R.id.tv_slot_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvBuildingName = itemView.findViewById(R.id.tv_building_name);
            tvCountdown = itemView.findViewById(R.id.tv_countdown);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSlotClick(items.get(pos));
                }
            });
        }

        public void bind(SlotWithTask item) {
            tvSlotName.setText(item.slot.name);

            if (item.task != null) {
                // Busy
                tvStatus.setText("Working");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_busy));
                
                tvBuildingName.setVisibility(View.VISIBLE);
                tvCountdown.setVisibility(View.VISIBLE);
                tvBuildingName.setText("Upgrading: " + item.task.buildingName);
                
                long remaining = item.task.endTime - System.currentTimeMillis();
                if (remaining <= 0) {
                    tvCountdown.setText("Finished!");
                } else {
                    // Quick formatting for placeholder. Will update dynamically later.
                    long seconds = remaining / 1000;
                    long d = seconds / (24 * 3600);
                    long h = (seconds % (24 * 3600)) / 3600;
                    long m = (seconds % 3600) / 60;
                    tvCountdown.setText(String.format("%dd %dh %dm remaining", d, h, m));
                }
            } else {
                // Available
                tvStatus.setText("Available");
                tvStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.status_available));
                
                tvBuildingName.setVisibility(View.GONE);
                tvCountdown.setVisibility(View.GONE);
            }
        }
    }
}
