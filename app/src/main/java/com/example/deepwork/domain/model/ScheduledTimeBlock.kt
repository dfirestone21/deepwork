package com.example.deepwork.domain.model

import com.example.deepwork.domain.model.ScheduledTimeBlock.BlockType.BREAK
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

data class ScheduledTimeBlock(
    val id: Uuid,
    val duration: Duration,
    val type: BlockType,
    val status: BlockStatus,
    val categories: List<Category>,
    val position: Int,
    val createdAt: Long,
    val updatedAt: Long
) {

    object Durations {
        val DEEP_WORK_DURATION_MIN = 25.minutes
        val DEEP_WORK_DURATION_MAX = 120.minutes
        val SHALLOW_WORK_DURATION_MIN = 10.minutes
        val SHALLOW_WORK_DURATION_MAX = 60.minutes
        val BREAK_DURATION_MIN = 5.minutes
        val BREAK_DURATION_MAX = 60.minutes
    }

    enum class BlockType {
        DEEP_WORK,
        SHALLOW_WORK,
        BREAK;

        val isWorkBlock: Boolean
            get() = this != BREAK

        val isBreakBlock: Boolean
            get() = this == BREAK

        val requiresCategories: Boolean
            get() = isWorkBlock

        val minDuration: Duration
            get() = when (this) {
                DEEP_WORK -> Durations.DEEP_WORK_DURATION_MIN
                SHALLOW_WORK -> Durations.SHALLOW_WORK_DURATION_MIN
                BREAK -> Durations.BREAK_DURATION_MIN
            }

        val maxDuration: Duration
            get() = when (this) {
                DEEP_WORK -> Durations.DEEP_WORK_DURATION_MAX
                SHALLOW_WORK -> Durations.SHALLOW_WORK_DURATION_MAX
                BREAK -> Durations.BREAK_DURATION_MAX
            }
    }

    val isWorkBlock = type.isWorkBlock
    val isBreakBlock = type.isBreakBlock

    enum class BlockStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED
    }

    companion object {

        const val CATEGORIES_MAX = 3

        fun deepWorkBlock(
            duration: Duration = Durations.DEEP_WORK_DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT),
            position: Int = 0
        ): ScheduledTimeBlock {
            return ScheduledTimeBlock(
                id = Uuid.random(),
                duration = duration,
                type = BlockType.DEEP_WORK,
                status = BlockStatus.NOT_STARTED,
                categories = categories,
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun shallowWorkBlock(
            duration: Duration = Durations.SHALLOW_WORK_DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT),
            position: Int = 0
        ): ScheduledTimeBlock {
            return ScheduledTimeBlock(
                id = Uuid.random(),
                duration = duration,
                type = BlockType.SHALLOW_WORK,
                status = BlockStatus.NOT_STARTED,
                categories = categories,
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun breakBlock(
            duration: Duration = Durations.BREAK_DURATION_MIN,
            position: Int = 0
        ): ScheduledTimeBlock {
            return ScheduledTimeBlock(
                id = Uuid.random(),
                duration = duration,
                type = BlockType.BREAK,
                status = BlockStatus.NOT_STARTED,
                categories = emptyList(),
                position = position,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }
    }
}