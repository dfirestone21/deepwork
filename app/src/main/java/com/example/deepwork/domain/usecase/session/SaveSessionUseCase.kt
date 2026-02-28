package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.repository.SessionRepository
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val validateSessionName: ValidateSessionNameUseCase
) {

    suspend operator fun invoke(session: ScheduledSession): Result<ScheduledSession> {
        val nameResult = validateSessionName(session.name)
        if (nameResult is Result.Error) {
            return nameResult
        }

        if (session.timeBlocks.isEmpty()) {
            return Result.Error(SessionException.MinTimeBlocksReached())
        }

        return try {
            val saved = sessionRepository.save(session)
            Result.Success(saved)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
