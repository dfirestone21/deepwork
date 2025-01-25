package com.example.deepwork.data.database.room.model.session_template

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.deepwork.data.database.room.model.category.CategoryEntity
import java.util.UUID

@Entity(
    tableName = "template_time_block_category_cross_ref",
    primaryKeys = ["timeblock_template_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = TimeBlockTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["timeblock_template_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["timeblock_template_id"]),
        Index(value = ["category_id"])
    ]
)
data class TimeBlockTemplateCategoryCrossRef(
    @ColumnInfo(name = "timeblock_template_id")
    val timeBlockTemplateId: UUID,

    @ColumnInfo(name = "category_id")
    val categoryId: UUID,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long?
)
