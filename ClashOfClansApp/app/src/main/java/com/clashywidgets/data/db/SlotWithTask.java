package com.clashywidgets.data.db;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SlotWithTask {
    @Embedded
    public WorkerSlot slot;

    @Relation(
            parentColumn = "id",
            entityColumn = "slotId"
    )
    public UpgradeTask task; // Will be null if no upgrade is currently happening
}
