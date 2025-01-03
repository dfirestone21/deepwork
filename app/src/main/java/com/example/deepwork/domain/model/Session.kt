package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class Session(
    val id: String,
    val name: String,
    val description: String?,
    val timeBlocks: List<TimeBlock>,
) {
    val totalDuration = timeBlocks.sumOf { it.duration.inWholeMilliseconds }.milliseconds

    companion object {
        const val MAX_TIME_BLOCKS = 12
        val MAX_DURATION: Duration = 12.hours
        val DURATION_MAX_CONSECUTIVE_DEEP_WORK: Duration = 2.5.hours

        fun create(name: String): Session {
            return Session(
                id = "",
                name = name,
                description = null,
                timeBlocks = emptyList()
            )
        }
    }
}
