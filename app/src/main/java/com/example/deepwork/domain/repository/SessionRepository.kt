package com.example.deepwork.domain.repository

import com.example.deepwork.domain.model.ScheduledSession

interface SessionRepository {
    suspend fun save(session: ScheduledSession): ScheduledSession
}
