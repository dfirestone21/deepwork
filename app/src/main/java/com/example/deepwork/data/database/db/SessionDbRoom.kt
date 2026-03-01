package com.example.deepwork.data.database.db

import com.example.deepwork.data.database.room.model.scheduled_session.ScheduledSessionEntity
import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.ScheduledSession
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import kotlin.uuid.toJavaUuid

class SessionDbRoom @Inject constructor(
    private val sessionDao: SessionDao
) : SessionDb {

    override suspend fun save(session: ScheduledSession): ScheduledSession {
        val preparedSession = prepareSessionForSave(session)
        val entity = preparedSession.toEntity()
        try {
            sessionDao.upsert(entity)
        } catch (e: Exception) {
            Timber.d("upsert session failed: ${e.message}")
            throw DatabaseException("Error saving session: ${e.message}")
        }
        return preparedSession
    }

    private fun prepareSessionForSave(session: ScheduledSession): ScheduledSession {
        val hasBeenSavedBefore = session.createdAt > 0
        val currentTime = Calendar.getInstance().timeInMillis
        return if (hasBeenSavedBefore) {
            session.copy(updatedAt = currentTime)
        } else {
            session.copy(createdAt = currentTime)
        }
    }

    private fun ScheduledSession.toEntity(): ScheduledSessionEntity {
        return ScheduledSessionEntity(
            id = id.toJavaUuid(),
            templateId = null,
            name = name,
            description = description,
            scheduledStartTime = scheduledStartTime,
            actualStartTime = actualStartTime,
            actualEndTime = actualEndTime,
            status = status.name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
