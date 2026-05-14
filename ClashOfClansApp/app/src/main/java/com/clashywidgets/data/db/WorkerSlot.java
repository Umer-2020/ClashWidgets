package com.clashywidgets.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "worker_slots")
public class WorkerSlot {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String name;
    public String village; // "HOME" or "BUILDER"
    public String type; // "BUILDER", "LAB", "PET"
}
