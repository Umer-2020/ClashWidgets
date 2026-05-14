package com.clashywidgets.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UpgradeTaskDao {
    @Query("SELECT * FROM upgrade_tasks WHERE slotId = :slotId LIMIT 1")
    LiveData<UpgradeTask> getTaskForSlot(int slotId);

    @Query("SELECT * FROM upgrade_tasks")
    LiveData<List<UpgradeTask>> getAllTasks();

    @Query("SELECT * FROM upgrade_tasks")
    List<UpgradeTask> getAllTasksSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UpgradeTask task);

    @Update
    void update(UpgradeTask task);

    @Query("DELETE FROM upgrade_tasks WHERE id = :taskId")
    void deleteTask(int taskId);

    @Query("DELETE FROM upgrade_tasks WHERE slotId = :slotId")
    void deleteTasksForSlot(int slotId);
}
