package com.example.deepwork.data.repository

import com.example.deepwork.data.database.db.SessionDb
import com.example.deepwork.domain.exception.DatabaseException
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var sessionDb: SessionDb
    private lateinit var repository: SessionRepository

    private val session = ScheduledSession(
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
        sessionDb = mockk()
        repository = SessionRepositoryImpl(sessionDb)
    }

    @Test
    fun `save() when called with a valid session, should delegate to SessionDb save() and return the result`() = runTest(dispatcher) {
        // given
        val savedSession = session.copy(createdAt = System.currentTimeMillis())
        coEvery { sessionDb.save(session) } returns savedSession

        // when
        val result = repository.save(session)
        advanceUntilIdle()

        // then
        assertEquals(savedSession, result)
        coVerify(exactly = 1) { sessionDb.save(session) }
    }

    @Test
    fun `save() when SessionDb throws an exception, should propagate the exception to the caller`() = runTest(dispatcher) {
        // given
        val dbException = DatabaseException("DB error")
        coEvery { sessionDb.save(session) } throws dbException

        // when
        var thrownException: Exception? = null
        try {
            repository.save(session)
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
