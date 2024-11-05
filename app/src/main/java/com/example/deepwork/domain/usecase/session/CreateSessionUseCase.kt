package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import java.util.UUID
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor() {

    companion object {
        internal const val NAME_MAX_LENGTH = 50
        private val NAME_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9 _-]*$".toRegex()
        private val CONSECUTIVE_SPACES = "\\s{2,}".toRegex()
    }

    suspend operator fun invoke(session: Session): Result<Session> {
        return try {
            validate(session)
            val preparedSession = prepare(session)
            Result.Success(preparedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(session: Session) {
        when {
            session.name.isBlank() -> throw SessionException.InvalidName("Session name cannot be empty")
            session.name.length > NAME_MAX_LENGTH -> throw SessionException.InvalidName("Session name cannot be longer than $NAME_MAX_LENGTH characters")
            !session.name.matches(NAME_PATTERN) -> throw SessionException.InvalidName("Session name must start with a letter or number and can only contain letters, numbers, spaces, hyphens, or underscores")
            session.name.contains(CONSECUTIVE_SPACES) -> throw SessionException.InvalidName("Session name cannot contain consecutive spaces")
        }
    }

    private fun prepare(session: Session): Session {
        return session.copy(
            id = session.id.ifBlank { UUID.randomUUID().toString() }
        )
    }
}