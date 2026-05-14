package com.clashywidgets.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "upgrade_tasks")
public class UpgradeTask {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int slotId;
    public String buildingName;
    public long endTime; // Timestamp
}
