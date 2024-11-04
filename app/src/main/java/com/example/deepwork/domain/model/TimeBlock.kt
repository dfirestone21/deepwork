package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

sealed class TimeBlock(
    open val id: String,
    open val duration: Duration
) {

    companion object {

        fun workBlock(
            duration: Duration = WorkBlock.DURATION_MIN,
            categories: List<Category> = listOf(Category.DEFAULT)
        ): WorkBlock {
            return WorkBlock(
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

    data class WorkBlock(
        override val id: String,
        override val duration: Duration,
        val categories: List<Category>
    ) : TimeBlock(id, duration) {

        companion object {
            val DURATION_MIN: Duration = 25.minutes
            val DURATION_MAX: Duration = 120.minutes
            const val CATEGORIES_MAX = 3
        }
    }

    data class BreakBlock(
        override val id: String,
        override val duration: Duration
    ) : TimeBlock(id, duration) {

        companion object {
            val DURATION_MIN: Duration = 5.minutes
            val DURATION_MAX: Duration = 60.minutes
        }
    }
}