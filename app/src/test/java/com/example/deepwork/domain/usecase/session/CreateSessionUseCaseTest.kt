package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

class CreateSessionUseCaseTest {
    private lateinit var createSession: CreateSessionUseCase
    private lateinit var validateName: ValidateSessionNameUseCase

    @Before
    fun setup() {
        validateName = mockk()
        every { validateName(any()) } answers { Result.Success(firstArg()) }
        createSession = CreateSessionUseCase(validateName)
    }

    @Test
    fun `when session is created, should generate id`() = runTest {
        // given
        val session = createDefaultSession().copy(id = Uuid.NIL)
        // when
        val actualSession = createSession(session).getOrThrow()
        // then
        assert(actualSession.id != Uuid.NIL)
    }

    private fun createDefaultSession(): ScheduledSession {
        val timeBlocks = listOf(
            ScheduledTimeBlock.deepWorkBlock(),
            ScheduledTimeBlock.breakBlock()
        )
        return ScheduledSession.create(
            "Test Session",
            System.currentTimeMillis()
        ).copy(
            timeBlocks = timeBlocks
        )
    }


}