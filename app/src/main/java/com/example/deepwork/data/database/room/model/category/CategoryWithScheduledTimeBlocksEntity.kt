package com.example.deepwork.data.database.room.model.category

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledTimeBlockCategoryCrossRef
import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledTimeBlockEntity

data class CategoryWithScheduledTimeBlocksEntity(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "time_block_id",
        associateBy = Junction(
            value = ScheduledTimeBlockCategoryCrossRef::class,
            parentColumn = "category_id",
            entityColumn = "time_block_id"
        )
    )
    val timeBlocks: List<ScheduledTimeBlockEntity>
)
