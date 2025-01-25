package com.example.deepwork.data.database.room.model.scheduled_session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.deepwork.data.database.room.model.session_template.TimeBlockTemplateEntity
import java.util.UUID

@Entity(
    tableName = "scheduled_time_block",
    foreignKeys = [
        ForeignKey(
            entity = ScheduledSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TimeBlockTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_block_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["template_block_id"]),
        Index(value = ["session_id", "position"], unique = true)
    ]
)
data class ScheduledTimeBlockEntity(
    @PrimaryKey
    val id: UUID,

    @ColumnInfo(name = "session_id")
    val sessionId: UUID,

    @ColumnInfo(name = "template_block_id")
    val templateBlockId: UUID,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Long,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "block_type")
    val blockType: String,

    @ColumnInfo(name = "actual_start_time")
    val actualStartTime: Long?,

    @ColumnInfo(name = "actual_end_time")
    val actualEndTime: Long?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

//    companion object {
//
//        const val TABLE_NAME = "timeblock"
//
//        fun toEntity(timeBlock: TimeBlock): TimeBlockEntity {
//            return TimeBlockEntity(
//                id = timeBlock.id.toJavaUuid(),
//                durationMinutes = timeBlock.duration.inWholeMinutes,
//                blockType = timeBlock.blockType.name,
//                createdAt = timeBlock.createdAt,
//                updatedAt = timeBlock.updatedAt
//            )
//        }
//    }
//
//    fun toDomain(): TimeBlock {
//        val type = TimeBlock.BlockType.valueOf(blockType)
//        return when (type) {
//            TimeBlock.BlockType.DEEP -> TimeBlock.WorkBlock.DeepWorkBlock(
//                id = id.toKotlinUuid(),
//                duration = durationMinutes.minutes,
//                createdAt = createdAt,
//                updatedAt = updatedAt,
//                categories = emptyList()
//            )
//            TimeBlock.BlockType.SHALLOW -> TimeBlock.WorkBlock.ShallowWorkBlock(
//                id = id.toKotlinUuid(),
//                duration = durationMinutes.minutes,
//                createdAt = createdAt,
//                updatedAt = updatedAt,
//                categories = emptyList()
//            )
//            TimeBlock.BlockType.BREAK -> TimeBlock.BreakBlock(
//                id = id.toKotlinUuid(),
//                duration = durationMinutes.minutes,
//                createdAt = createdAt,
//                updatedAt = updatedAt
//            )
//        }
//    }
//}
