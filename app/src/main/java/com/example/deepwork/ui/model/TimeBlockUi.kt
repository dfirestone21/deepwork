package com.example.deepwork.ui.model

import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.TimeBlock
import kotlin.time.Duration

data class TimeBlockUi(
    val duration: Duration,
    val categories: List<Category>,
    val type: Type
) {

    val durationFormatted: String get() = duration.toString()

    companion object {

        fun fromDomain(timeBlock: TimeBlock): TimeBlockUi {
            return TimeBlockUi(
                duration = timeBlock.duration,
                categories = categoriesFor(timeBlock),
                type = typeFrom(timeBlock)
            )
        }

        private fun categoriesFor(timeBlock: TimeBlock): List<Category> {
            return when (timeBlock) {
                is TimeBlock.WorkBlock -> timeBlock.categories
                else -> emptyList()
            }
        }

        private fun typeFrom(timeBlock: TimeBlock): Type {
            return when (timeBlock) {
                is TimeBlock.WorkBlock.DeepWorkBlock -> Type.DEEP_WORK
                is TimeBlock.WorkBlock.ShallowWorkBlock -> Type.SHALLOW_WORK
                is TimeBlock.BreakBlock -> Type.BREAK
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
