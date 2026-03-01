package com.example.deepwork.data.repository

import com.example.deepwork.data.database.db.SessionDb
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.repository.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val db: SessionDb
) : SessionRepository {

    override suspend fun save(session: ScheduledSession): ScheduledSession {
        return db.save(session)
    }
}
