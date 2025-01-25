package com.example.deepwork.data.database.room.model.session_template

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "template_time_block",
    foreignKeys = [
        ForeignKey(
            entity = SessionTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_template_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_template_id"]),
        Index(value = ["session_template_id", "position"], unique = true)
    ]
)
data class TimeBlockTemplateEntity(
    @PrimaryKey
    val id: UUID,

    @ColumnInfo(name = "session_template_id")
    val sessionTemplateId: UUID,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Long,

    @ColumnInfo(name = "block_type")
    val blockType: String,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long?
)
