package com.example.deepwork.data.database.room.model.scheduled_session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import java.util.UUID

@Entity(
    tableName = "scheduled_time_block_category_cross_ref",
    primaryKeys = ["time_block_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = ScheduledTimeBlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["time_block_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["time_block_id"]),
        Index(value = ["category_id"])
    ]
)
data class ScheduledTimeBlockCategoryCrossRef(
    @ColumnInfo("time_block_id")
    val timeBlockId: UUID,

    @ColumnInfo("category_id")
    val categoryId: UUID,

    @ColumnInfo("created_at")
    val createdAt: Long,

    @ColumnInfo("updated_at")
    val updatedAt: Long?
)
