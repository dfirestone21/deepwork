package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.repository.SessionRepository
import com.example.deepwork.domain.usecase.session.validate.ValidateSessionNameUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveSessionUseCaseTest {

    private lateinit var saveSession: SaveSessionUseCase
    private lateinit var sessionRepository: SessionRepository
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        sessionRepository = mockk()
        saveSession = SaveSessionUseCase(sessionRepository, ValidateSessionNameUseCase())
    }

    // -------------------------------------------------------------------------
    // Test 1: Valid session with at least one work block — happy path
    // -------------------------------------------------------------------------

    @Test
    fun `when saving a valid session with a work block, should call repository and return Success with saved session`() = runTest {
        // given
        val session = createSessionWithBlocks(ScheduledTimeBlock.deepWorkBlock())
        coEvery { sessionRepository.save(session) } returns session

        // when
        val result = saveSession(session)

        // then
        assertTrue(result is Result.Success)
        assertEquals(session, result.getOrThrow())
        coVerify(exactly = 1) { sessionRepository.save(session) }
    }

    // -------------------------------------------------------------------------
    // Test 2: Invalid session name — no repository call, returns InvalidName error
    // -------------------------------------------------------------------------

    @Test
    fun `when session name is blank, should return InvalidName error without calling repository`() = runTest {
        // given
        val session = createSessionWithBlocks(ScheduledTimeBlock.deepWorkBlock()).copy(name = "")

        // when
        val result = saveSession(session)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected InvalidName but got ${error.exception}",
            error.exception is SessionException.InvalidName
        )
        coVerify(exactly = 0) { sessionRepository.save(any()) }
    }

    @Test
    fun `when session name exceeds max length, should return InvalidName error without calling repository`() = runTest {
        // given
        val tooLongName = "A".repeat(51)
        val session = createSessionWithBlocks(ScheduledTimeBlock.deepWorkBlock()).copy(name = tooLongName)

        // when
        val result = saveSession(session)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected InvalidName but got ${error.exception}",
            error.exception is SessionException.InvalidName
        )
        coVerify(exactly = 0) { sessionRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // Test 3: Session has no time blocks — returns MinTimeBlocksReached error
    // -------------------------------------------------------------------------

    @Test
    fun `when session has no time blocks, should return MinTimeBlocksReached error without calling repository`() = runTest {
        // given
        val session = ScheduledSession.create(
            name = "Valid Name",
            startTime = System.currentTimeMillis()
        ) // timeBlocks = emptyList() by default

        // when
        val result = saveSession(session)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected MinTimeBlocksReached but got ${error.exception}",
            error.exception is SessionException.MinTimeBlocksReached
        )
        coVerify(exactly = 0) { sessionRepository.save(any()) }
    }

    // -------------------------------------------------------------------------
    // Test 4: Repository throws DatabaseException — use case wraps it in Result.Error
    // -------------------------------------------------------------------------

    @Test
    fun `when repository throws DatabaseException, should return Error with that exception`() = runTest {
        // given
        val session = createSessionWithBlocks(ScheduledTimeBlock.deepWorkBlock())
        val dbException = DatabaseException("Failed to write to database")
        coEvery { sessionRepository.save(session) } throws dbException

        // when
        val result = saveSession(session)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected DatabaseException but got ${error.exception}",
            error.exception is DatabaseException
        )
        assertEquals("Failed to write to database", (error.exception as DatabaseException).message)
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun createSessionWithBlocks(vararg blocks: ScheduledTimeBlock): ScheduledSession {
        return ScheduledSession.create(
            name = "Test Session",
            startTime = System.currentTimeMillis()
        ).copy(
            timeBlocks = blocks.toList()
        )
    }
}
