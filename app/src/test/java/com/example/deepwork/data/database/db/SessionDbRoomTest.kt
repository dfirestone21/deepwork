package com.example.deepwork.data.database.db

import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.Category
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SessionDbRoomTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: SessionDao
    private lateinit var sessionDb: SessionDb

    private val baseSession = ScheduledSession(
        id = Uuid.random(),
        name = "Test Session",
        description = null,
        timeBlocks = emptyList(),
        status = ScheduledSession.SessionStatus.NOT_STARTED,
        scheduledStartTime = 1_000_000L,
        actualStartTime = null,
        actualEndTime = null,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dao = mockk()
        sessionDb = SessionDbRoom(dao)
    }

    @Test
    fun `save() when called with a new session (createdAt == 0L), should set createdAt to current time and return the session`() = runTest(dispatcher) {
        // given
        val newSession = baseSession.copy(createdAt = 0L, updatedAt = 0L)
        coEvery { dao.upsert(any()) } answers { firstArg() }

        // when
        val result = sessionDb.save(newSession)
        advanceUntilIdle()

        // then
        assertTrue("createdAt should be set to a non-zero value", result.createdAt > 0L)
    }

    @Test
    fun `save() when called with an existing session (createdAt gt 0L), should set updatedAt to current time and return the session`() = runTest(dispatcher) {
        // given
        val existingSession = baseSession.copy(createdAt = 1_000_000L, updatedAt = 0L)
        coEvery { dao.upsert(any()) } answers { firstArg() }

        // when
        val result = sessionDb.save(existingSession)
        advanceUntilIdle()

        // then
        assertTrue("updatedAt should be set to a non-zero value", result.updatedAt > 0L)
    }

    @Test
    fun `save() when DAO throws an exception, should throw DatabaseException`() = runTest(dispatcher) {
        // given
        val newSession = baseSession.copy(createdAt = 0L, updatedAt = 0L)
        coEvery { dao.upsert(any()) } throws RuntimeException("DB failure")

        // when
        var thrownException: Exception? = null
        try {
            sessionDb.save(newSession)
            advanceUntilIdle()
        } catch (e: Exception) {
            thrownException = e
        }

        // then
        assertTrue(
            "Expected DatabaseException but got ${thrownException?.javaClass?.simpleName}",
            thrownException is DatabaseException
        )
    }
}
