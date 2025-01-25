package com.example.deepwork.domain.model.template

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import kotlin.time.Duration
import kotlin.uuid.Uuid

data class TimeBlockTemplate(
    val id: Uuid,
    val duration: Duration,
    val type: ScheduledTimeBlock.BlockType,
    val categories: List<Category>,
    val position: Int,
    val createdAt: Long,
    val updatedAt: Long
) {

    companion object {
        const val MAX_CATEGORIES = 3

        fun deepWorkTemplate(
            duration: Duration,
            categories: List<Category> = listOf(Category.DEFAULT),
            position: Int = 0
        ): TimeBlockTemplate {
            return TimeBlockTemplate(
                id = Uuid.random(),
                duration = duration,
                type = ScheduledTimeBlock.BlockType.DEEP_WORK,
                categories = categories,
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun shallowWorkTemplate(
            duration: Duration,
            categories: List<Category> = listOf(Category.DEFAULT),
            position: Int = 0
        ): TimeBlockTemplate {
            return TimeBlockTemplate(
                id = Uuid.random(),
                duration = duration,
                type = ScheduledTimeBlock.BlockType.SHALLOW_WORK,
                categories = categories,
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun breakTemplate(
            duration: Duration,
            position: Int = 0
        ): TimeBlockTemplate {
            return TimeBlockTemplate(
                id = Uuid.random(),
                duration = duration,
                type = ScheduledTimeBlock.BlockType.BREAK,
                categories = emptyList(),
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }
    }
}
