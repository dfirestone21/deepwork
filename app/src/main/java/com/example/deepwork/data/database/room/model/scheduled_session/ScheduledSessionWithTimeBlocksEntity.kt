package com.example.deepwork.data.database.room.model.scheduled_session

import androidx.room.Embedded
import androidx.room.Relation

data class ScheduledSessionWithTimeBlocksEntity(
    @Embedded
    val scheduledSession: ScheduledSessionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id"
    )
    val timeBlocks: List<ScheduledTimeBlockEntity>
)
