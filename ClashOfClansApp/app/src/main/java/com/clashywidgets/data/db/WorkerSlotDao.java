package com.clashywidgets.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import androidx.room.Transaction;

@Dao
public interface WorkerSlotDao {
    @Transaction
    @Query("SELECT * FROM worker_slots WHERE village = :village AND type = :type ORDER BY id ASC")
    LiveData<List<SlotWithTask>> getSlotsWithTasks(String village, String type);

    @Transaction
    @Query("SELECT * FROM worker_slots WHERE village = :village ORDER BY id ASC")
    LiveData<List<SlotWithTask>> getSlotsWithTasksByVillage(String village);

    @Query("SELECT * FROM worker_slots WHERE village = :village AND type = :type ORDER BY id ASC")
    LiveData<List<WorkerSlot>> getSlots(String village, String type);

    @Query("SELECT * FROM worker_slots WHERE village = :village ORDER BY id ASC")
    LiveData<List<WorkerSlot>> getSlotsByVillage(String village);

    @Query("SELECT * FROM worker_slots ORDER BY id ASC")
    LiveData<List<WorkerSlot>> getAllSlots();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WorkerSlot> slots);

    @androidx.room.Update
    void update(WorkerSlot slot);

    @Query("SELECT * FROM worker_slots ORDER BY id ASC")
    List<WorkerSlot> getAllSlotsSync();
}
