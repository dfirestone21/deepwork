package com.example.deepwork.domain.usecase.session.validate

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import javax.inject.Inject

class ValidateSessionNameUseCase @Inject constructor() {

    companion object {
        internal const val NAME_MAX_LENGTH = 50
        private val NAME_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9 _-]*$".toRegex()
        private val CONSECUTIVE_SPACES = "\\s{2,}".toRegex()
    }

    operator fun invoke(name: String): Result<String> {
        return try {
            validate(name)
            Result.Success(name)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validate(name: String) {
        when {
            name.isBlank() -> throw SessionException.InvalidName("Session name cannot be empty")
            name.length > NAME_MAX_LENGTH -> throw SessionException.InvalidName("Session name cannot be longer than $NAME_MAX_LENGTH characters")
            !name.matches(NAME_PATTERN) -> throw SessionException.InvalidName("Session name must start with a letter or number and can only contain letters, numbers, spaces, hyphens, or underscores")
            name.contains(CONSECUTIVE_SPACES) -> throw SessionException.InvalidName("Session name cannot contain consecutive spaces")
        }
    }
}