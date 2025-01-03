package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import java.util.UUID
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val validateName: ValidateSessionNameUseCase
) {

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
        validateName(session.name).getOrThrow()
    }

    private fun prepare(session: Session): Session {
        return session.copy(
            id = session.id.ifBlank { UUID.randomUUID().toString() }
        )
    }
}