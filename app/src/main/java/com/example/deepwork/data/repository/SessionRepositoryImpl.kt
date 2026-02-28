package com.example.deepwork.data.repository

import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.repository.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor() : SessionRepository {

    override suspend fun save(session: ScheduledSession): ScheduledSession {
        TODO("Not yet implemented")
    }
}
