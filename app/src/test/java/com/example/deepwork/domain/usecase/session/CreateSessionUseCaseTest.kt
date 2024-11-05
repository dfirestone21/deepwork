package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateSessionUseCaseTest {
    private lateinit var createSession: CreateSessionUseCase

    @Before
    fun setup() {
        createSession = CreateSessionUseCase()
    }

    @Test
    fun `when session is created, should generate id`() = runTest {
        // given
        val session = createDefaultSession().copy(id = "")
        // when
        val actualSession = createSession(session).getOrThrow()
        // then
        assert(actualSession.id.isNotEmpty())
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name is empty, should return error result`() = runTest {
        // given
        val session = createDefaultSession().copy(name = "")
        // when
        createSession(session).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name is longer than NAME_MAX_LENGTH, should return error result`() = runTest {
        // given
        val session = createDefaultSession().copy(name = "a".repeat(CreateSessionUseCase.NAME_MAX_LENGTH + 1))
        // when
        createSession(session).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name starts with special character, should return error result`() = runTest {
        // given
        val session = createDefaultSession().copy(name = "-InvalidStart")
        // when
        createSession(session).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name contains disallowed characters, should return error result`() = runTest {
        // given
        val session = createDefaultSession().copy(name = "Invalid@Name#Here")
        // when
        createSession(session).getOrThrow()
    }

    @Test(expected = SessionException.InvalidName::class)
    fun `when name contains consecutive spaces, should return error result`() = runTest {
        // given
        val session = createDefaultSession().copy(name = "Invalid  Name")
        // when
        createSession(session).getOrThrow()
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
            val session = createDefaultSession().copy(name = name)
            createSession(session).getOrThrow() // Should not throw
        }
    }

    private fun createDefaultSession(): Session {
        val timeBlocks = listOf(
            TimeBlock.workBlock(),
            TimeBlock.breakBlock()
        )
        return Session(
            id = "",
            name = "Default Session",
            description = null,
            timeBlocks = timeBlocks
        )
    }


}