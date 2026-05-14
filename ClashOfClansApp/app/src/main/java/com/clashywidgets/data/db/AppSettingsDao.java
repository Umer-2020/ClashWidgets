package com.clashywidgets.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AppSettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    LiveData<String> getSettingLiveData(String key);

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    String getSettingSync(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AppSettings setting);
}
