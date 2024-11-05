package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class Session(
    val id: String,
    val name: String,
    val description: String?,
    val timeBlocks: List<TimeBlock>,
) {

    companion object {
        const val MAX_TIME_BLOCKS = 12
        val MAX_DURATION: Duration = 12.hours
    }
}
