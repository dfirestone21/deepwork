package com.example.deepwork.data.database.room.model.scheduled_session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.deepwork.data.database.room.model.session_template.SessionTemplateEntity
import java.util.UUID

@Entity(
    tableName = "scheduled_session",
    foreignKeys = [
        ForeignKey(
            entity = SessionTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["template_id"]),
        Index(value = ["scheduled_start_time"]),
    ]
)
data class ScheduledSessionEntity(
    @PrimaryKey
    val id: UUID,

    @ColumnInfo(name = "template_id")
    val templateId: UUID,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "scheduled_start_time")
    val scheduledStartTime: Long,

    @ColumnInfo(name = "actual_start_time")
    val actualStartTime: Long?,

    @ColumnInfo(name = "actual_end_time")
    val actualEndTime: Long?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
