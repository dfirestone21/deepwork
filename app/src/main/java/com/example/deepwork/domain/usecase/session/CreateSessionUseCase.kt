package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import java.util.UUID
import javax.inject.Inject
import kotlin.uuid.Uuid

class CreateSessionUseCase @Inject constructor(
    private val validateName: ValidateSessionNameUseCase
) {

    suspend operator fun invoke(session: ScheduledSession): Result<ScheduledSession> {
        return try {
            validate(session)
            val preparedSession = prepare(session)
            Result.Success(preparedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(session: ScheduledSession) {
        validateName(session.name).getOrThrow()
    }

    private fun prepare(session: ScheduledSession): ScheduledSession {
        return session.copy(
            id = if (session.id == Uuid.NIL) Uuid.random() else session.id,
        )
    }
}