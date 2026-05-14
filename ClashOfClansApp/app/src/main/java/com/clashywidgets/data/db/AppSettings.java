package com.clashywidgets.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettings {
    @PrimaryKey
    @NonNull
    public String key;
    
    public String value;
    
    public AppSettings(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }
}
