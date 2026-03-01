package com.example.deepwork.data.database.db

import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionEntity

interface SessionDao {

    suspend fun upsert(session: ScheduledSessionEntity): ScheduledSessionEntity
}
