package com.example.deepwork.data.database.room.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.toKotlinUuid

data class TimeBlockWithCategoriesEntity(
    @Embedded val timeBlock: TimeBlockEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TimeBlockCategoryCrossRef::class,
            parentColumn = "timeBlockId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<CategoryEntity>
) {

    companion object {

        fun toEntity(timeBlock: TimeBlock): TimeBlockWithCategoriesEntity {
            val categories = if (timeBlock is TimeBlock.WorkBlock) {
                timeBlock.categories
            } else {
                emptyList()
            }
            return TimeBlockWithCategoriesEntity(
                timeBlock = TimeBlockEntity.toEntity(timeBlock),
                categories = categories.map { CategoryEntity.toEntity(it) }
            )
        }
    }

    fun toDomain(): TimeBlock {
        val type = TimeBlock.BlockType.valueOf(timeBlock.blockType)
        return when (type) {
            TimeBlock.BlockType.DEEP -> TimeBlock.WorkBlock.DeepWorkBlock(
                id = timeBlock.id.toKotlinUuid(),
                duration = timeBlock.durationMinutes.minutes,
                createdAt = timeBlock.createdAt,
                updatedAt = timeBlock.updatedAt,
                categories = categories.map { it.toDomain() }
            )
            TimeBlock.BlockType.SHALLOW -> TimeBlock.WorkBlock.ShallowWorkBlock(
                id = timeBlock.id.toKotlinUuid(),
                duration = timeBlock.durationMinutes.minutes,
                createdAt = timeBlock.createdAt,
                updatedAt = timeBlock.updatedAt,
                categories = categories.map { it.toDomain() }
            )
            TimeBlock.BlockType.BREAK -> TimeBlock.BreakBlock(
                id = timeBlock.id.toKotlinUuid(),
                duration = timeBlock.durationMinutes.minutes,
                createdAt = timeBlock.createdAt,
                updatedAt = timeBlock.updatedAt
            )
        }
    }
}