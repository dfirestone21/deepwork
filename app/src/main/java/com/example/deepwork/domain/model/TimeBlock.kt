package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

sealed class TimeBlock(
    open val id: Uuid,
    open val duration: Duration,
    open val createdAt: Long,
    open val updatedAt: Long
) {
    abstract val minDuration: Duration
    abstract val maxDuration: Duration
    abstract val blockType: BlockType

    enum class BlockType {
        DEEP,
        SHALLOW,
        BREAK
    }

    companion object {

        fun deepWorkBlock(
            duration: Duration = WorkBlock.DeepWorkBlock.DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT)
        ): WorkBlock {
            return WorkBlock.DeepWorkBlock(
                id = Uuid.random(),
                duration = duration,
                categories = categories,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun shallowWorkBlock(
            duration: Duration = WorkBlock.ShallowWorkBlock.DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT)
        ): WorkBlock {
            return WorkBlock.ShallowWorkBlock(
                id = Uuid.random(),
                duration = duration,
                categories = categories,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun breakBlock(duration: Duration = BreakBlock.DURATION_MIN): BreakBlock {
            return BreakBlock(
                id = Uuid.random(),
                duration = duration,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }

        fun minDuration(blockType: BlockType): Duration {
            return when (blockType) {
                BlockType.DEEP -> WorkBlock.DeepWorkBlock.DURATION_MIN
                BlockType.SHALLOW -> WorkBlock.ShallowWorkBlock.DURATION_MIN
                BlockType.BREAK -> BreakBlock.DURATION_MIN
            }
        }

        fun maxDuration(blockType: BlockType): Duration {
            return when (blockType) {
                BlockType.DEEP -> WorkBlock.DeepWorkBlock.DURATION_MAX
                BlockType.SHALLOW -> WorkBlock.ShallowWorkBlock.DURATION_MAX
                BlockType.BREAK -> BreakBlock.DURATION_MAX
            }
        }
    }

    abstract fun copyObject(
        id: Uuid = this.id,
        duration: Duration = this.duration,
        createdAt: Long = this.createdAt,
        updatedAt: Long = this.updatedAt
    ): TimeBlock

    sealed class WorkBlock(
        override val id: Uuid,
        override val duration: Duration,
        override val createdAt: Long,
        override val updatedAt: Long,
        open val categories: List<Category>
    ) : TimeBlock(id, duration, createdAt, updatedAt) {

        companion object {
            const val CATEGORIES_MAX = 3
        }

        abstract fun copyObject(
            id: Uuid,
            duration: Duration,
            categories: List<Category>,
            createdAt: Long,
            updatedAt: Long
        ): WorkBlock

        data class DeepWorkBlock(
            override val id: Uuid,
            override val duration: Duration,
            override val createdAt: Long,
            override val updatedAt: Long,
            override val categories: List<Category>
        ) : WorkBlock(id, duration, createdAt, updatedAt, categories) {

            override val minDuration: Duration = DURATION_MIN
            override val maxDuration: Duration = DURATION_MAX
            override val blockType: BlockType = BlockType.DEEP

            companion object {
                val DURATION_MIN: Duration = 25.minutes
                val DURATION_MAX: Duration = 120.minutes
            }

            override fun copyObject(id: Uuid, duration: Duration, categories: List<Category>, createdAt: Long, updatedAt: Long): WorkBlock {
                return copy(id, duration, createdAt, updatedAt, categories)
            }

            override fun copyObject(id: Uuid, duration: Duration, createdAt: Long, updatedAt: Long): TimeBlock {
                return copy(id, duration, createdAt, updatedAt, categories)
            }
        }

        data class ShallowWorkBlock(
            override val id: Uuid,
            override val duration: Duration,
            override val createdAt: Long,
            override val updatedAt: Long,
            override val categories: List<Category>
        ) : WorkBlock(id, duration, createdAt, updatedAt, categories) {

            override val minDuration: Duration = DURATION_MIN
            override val maxDuration: Duration = DURATION_MAX
            override val blockType: BlockType = BlockType.SHALLOW

            companion object {
                val DURATION_MIN: Duration = 10.minutes
                val DURATION_MAX: Duration = 60.minutes
            }

            override fun copyObject(id: Uuid, duration: Duration, categories: List<Category>, createdAt: Long, updatedAt: Long): WorkBlock {
                return copy(id, duration, createdAt, updatedAt, categories)
            }

            override fun copyObject(id: Uuid, duration: Duration, createdAt: Long, updatedAt: Long): TimeBlock {
                return copy(id, duration, createdAt, updatedAt, categories)
            }
        }
    }

    data class BreakBlock(
        override val id: Uuid,
        override val duration: Duration,
        override val createdAt: Long,
        override val updatedAt: Long
    ) : TimeBlock(id, duration, createdAt, updatedAt) {

        override val minDuration: Duration = DURATION_MIN
        override val maxDuration: Duration = DURATION_MAX
        override val blockType: BlockType = BlockType.BREAK

        companion object {
            val DURATION_MIN: Duration = 5.minutes
            val DURATION_MAX: Duration = 60.minutes
        }

        override fun copyObject(id: Uuid, duration: Duration, createdAt: Long, updatedAt: Long): TimeBlock {
            return copy(id, duration, createdAt, updatedAt)
        }
    }
}