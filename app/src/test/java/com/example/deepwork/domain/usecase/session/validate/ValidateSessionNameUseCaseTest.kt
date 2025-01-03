package com.example.deepwork.domain.usecase.session.validate

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase.Companion.NAME_MAX_LENGTH
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ValidateSessionNameUseCaseTest {
    private lateinit var validateName: ValidateSessionNameUseCase
    
    @Before
    fun setup() {
        validateName = ValidateSessionNameUseCase()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name is empty, should return error result`() = runTest {
        // given
        val name = ""
        // when
        validateName(name).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name is longer than NAME_MAX_LENGTH, should return error result`() = runTest {
        // given
        val name = "a".repeat(NAME_MAX_LENGTH + 1)
        // when
        validateName(name).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name starts with special character, should return error result`() = runTest {
        // given
        val name = "-InvalidStart"
        // when
        validateName(name).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name contains disallowed characters, should return error result`() = runTest {
        // given
        val name = "Invalid@Name#Here"
        // when
        validateName(name).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name contains consecutive spaces, should return error result`() = runTest {
        // given
        val name = "Invalid  Name"
        // when
        validateName(name).getOrThrow()
    }

    @Test
    fun `when name follows all rules, should not throw error`() = runTest {
        // given
        val validNames = listOf(
            "Valid Name",
            "valid-name",
            "valid_name",
            "Valid123",
            "123Valid"
        )

        // when & then
        validNames.forEach { name ->
            validateName(name).getOrThrow() // Should not throw
        }
    }
}