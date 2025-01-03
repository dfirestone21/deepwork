package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

sealed class TimeBlock(
    open val id: String,
    open val duration: Duration
) {
    abstract val minDuration: Duration
    abstract val maxDuration: Duration

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
                id = "",
                duration = duration,
                categories = categories
            )
        }

        fun shallowWorkBlock(
            duration: Duration = WorkBlock.ShallowWorkBlock.DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT)
        ): WorkBlock {
            return WorkBlock.ShallowWorkBlock(
                id = "",
                duration = duration,
                categories = categories
            )
        }

        fun breakBlock(duration: Duration = BreakBlock.DURATION_MIN): BreakBlock {
            return BreakBlock(
                id = "",
                duration = duration
            )
        }
    }

    abstract fun copyValues(
        id: String = this.id,
        duration: Duration = this.duration
    ): TimeBlock

    sealed class WorkBlock(
        override val id: String,
        override val duration: Duration,
        open val categories: List<Category>
    ) : TimeBlock(id, duration) {

        companion object {
            const val CATEGORIES_MAX = 3
        }

        abstract fun copyValues(
            id: String,
            duration: Duration,
            categories: List<Category>
        ): WorkBlock

        data class DeepWorkBlock(
            override val id: String,
            override val duration: Duration,
            override val categories: List<Category>
        ) : WorkBlock(id, duration, categories) {

            override val minDuration: Duration = DURATION_MIN
            override val maxDuration: Duration = DURATION_MAX

            companion object {
                val DURATION_MIN: Duration = 25.minutes
                val DURATION_MAX: Duration = 120.minutes
            }

            override fun copyValues(id: String, duration: Duration, categories: List<Category>): WorkBlock {
                return copy(id, duration, categories)
            }

            override fun copyValues(id: String, duration: Duration): TimeBlock {
                return copy(id, duration, categories)
            }
        }

        data class ShallowWorkBlock(
            override val id: String,
            override val duration: Duration,
            override val categories: List<Category>
        ) : WorkBlock(id, duration, categories) {

            override val minDuration: Duration = DURATION_MIN
            override val maxDuration: Duration = DURATION_MAX

            companion object {
                val DURATION_MIN: Duration = 10.minutes
                val DURATION_MAX: Duration = 60.minutes
            }

            override fun copyValues(id: String, duration: Duration, categories: List<Category>): WorkBlock {
                return copy(id, duration, categories)
            }

            override fun copyValues(id: String, duration: Duration): TimeBlock {
                return copy(id, duration, categories)
            }
        }
    }

    data class BreakBlock(
        override val id: String,
        override val duration: Duration
    ) : TimeBlock(id, duration) {

        override val minDuration: Duration = DURATION_MIN
        override val maxDuration: Duration = DURATION_MAX

        companion object {
            val DURATION_MIN: Duration = 5.minutes
            val DURATION_MAX: Duration = 60.minutes
        }

        override fun copyValues(id: String, duration: Duration): TimeBlock {
            return copy(id, duration)
        }
    }
}