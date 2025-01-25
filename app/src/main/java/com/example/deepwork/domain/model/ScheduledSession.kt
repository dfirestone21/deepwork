package com.example.deepwork.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

data class ScheduledSession(
    val id: Uuid,
    val name: String,
    val description: String?,
    val timeBlocks: List<ScheduledTimeBlock>,
    val status: SessionStatus,
    val scheduledStartTime: Long,
    val actualStartTime: Long?,
    val actualEndTime: Long?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val totalDuration = timeBlocks.sumOf { it.duration.inWholeMinutes }.minutes

    enum class SessionStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    companion object {
        const val MAX_TIME_BLOCKS = 12
        val MAX_DURATION: Duration = 12.hours
        val DURATION_MAX_CONSECUTIVE_DEEP_WORK: Duration = 2.5.hours

        fun create(name: String, startTime: Long): ScheduledSession {
            return ScheduledSession(
                id = Uuid.random(),
                name = name,
                description = null,
                timeBlocks = emptyList(),
                status = SessionStatus.NOT_STARTED,
                scheduledStartTime = startTime,
                actualStartTime = null,
                actualEndTime = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = 0
            )
        }
    }
}
