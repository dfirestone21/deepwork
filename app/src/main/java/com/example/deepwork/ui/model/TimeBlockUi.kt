package com.example.deepwork.ui.model

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledTimeBlock
import kotlin.time.Duration

data class TimeBlockUi(
    val duration: Duration,
    val categories: List<Category>,
    val type: Type
) {

    val durationFormatted: String get() = duration.toString()

    companion object {

        fun fromDomain(timeBlock: ScheduledTimeBlock): TimeBlockUi {
            return TimeBlockUi(
                duration = timeBlock.duration,
                categories = timeBlock.categories,
                type = typeFrom(timeBlock)
            )
        }

        private fun typeFrom(timeBlock: ScheduledTimeBlock): Type {
            return when (timeBlock.type) {
                ScheduledTimeBlock.BlockType.DEEP_WORK -> Type.DEEP_WORK
                ScheduledTimeBlock.BlockType.SHALLOW_WORK -> Type.SHALLOW_WORK
                ScheduledTimeBlock.BlockType.BREAK -> Type.BREAK
            }
        }

        enum class Type {
            DEEP_WORK,
            SHALLOW_WORK,
            BREAK;

            override fun toString(): String {
                return when (this) {
                    DEEP_WORK -> "Deep Work"
                    SHALLOW_WORK -> "Shallow Work"
                    BREAK -> "Break"
                }
            }
        }
    }
}
