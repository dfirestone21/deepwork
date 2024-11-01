package com.example.deepwork.domain.model

import kotlin.time.Duration

sealed class TimeBlock(
    open val id: String,
    open val duration: Duration
) {
    data class WorkBlock(
        override val id: String,
        override val duration: Duration
    ) : TimeBlock(id, duration)

    data class BreakBlock(
        override val id: String,
        override val duration: Duration
    ) : TimeBlock(id, duration)
}