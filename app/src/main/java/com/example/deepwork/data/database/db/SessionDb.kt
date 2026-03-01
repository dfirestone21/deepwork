package com.example.deepwork.data.database.db

import com.example.deepwork.domain.model.ScheduledSession

interface SessionDb {
    suspend fun save(session: ScheduledSession): ScheduledSession
}
