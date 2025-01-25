package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class AddTimeBlockUseCaseTest {
    private lateinit var addTimeBlock: AddTimeBlockUseCase
    private var session: ScheduledSession = createDefaultSession()

    @Before
    fun setup() {
        addTimeBlock = AddTimeBlockUseCase()
    }

    @Test
    fun `when adding new work block, new session should contain work block`() = runTest {
        // given
        val block = ScheduledTimeBlock.deepWorkBlock()
        // when
        val updatedSession = addTimeBlock(session, block).getOrThrow()
        val actualBlock = updatedSession.timeBlocks.first()

        // then
        assertEquals(1, updatedSession.timeBlocks.size)
        assertEquals(block, actualBlock)
    }

    @Test(expected = SessionException.MaxTimeBlocksReached::class)
    fun `when session already has MAX_WORK_BLOCKS should throw exception`() = runTest {
        // given
        var session = createDefaultSession()
        for (i in 1..12) {
            val block = ScheduledTimeBlock.deepWorkBlock()
            val breakBlock = ScheduledTimeBlock.breakBlock()
            session = addTimeBlock(session, block).getOrThrow()
            session = addTimeBlock(session, breakBlock).getOrThrow()
        }
        val newBlock = ScheduledTimeBlock.deepWorkBlock()
        // when
        addTimeBlock(session, newBlock).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when adding a break block, new session should contain new break block`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, breakBlock).getOrThrow()
        val actualBreakBlock = updatedSession.timeBlocks.last()

        // then
        assertEquals(2, updatedSession.timeBlocks.size)
        assertEquals(breakBlock, actualBreakBlock)
    }

    @Test(expected = SessionException.InvalidBreakPosition::class)
    fun `when adding a break block before any work blocks have been added, should throw exception`() = runTest {
        // given
        val breakBlock = ScheduledTimeBlock.breakBlock()
        // when
        addTimeBlock(session, breakBlock).getOrThrow()

        // then
        // exception should be thrown
    }


    @Test(expected = SessionException.MaxSessionDurationReached::class)
    fun `when adding a block exceeds MAX_DURATION, should throw exception`() = runTest {
        // given
        val block = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )
        val break1 = ScheduledTimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block2 = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )
        val break2 = ScheduledTimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block3 = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )
        val break3 = ScheduledTimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block4 = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )
        val break4 = ScheduledTimeBlock.breakBlock(
            duration = 35.minutes
        )
        val block5 = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )

        // when
        var updatedSession = addTimeBlock(session, block).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, break1).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, block2).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, break2).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, block3).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, break3).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, block4).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, break4).getOrThrow()
        addTimeBlock(updatedSession, block5).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.ConsecutiveBlockTypes::class)
    fun `when adding a break block directly after another break block, should throw exception`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock1 = ScheduledTimeBlock.breakBlock()
        val breakBlock2 = ScheduledTimeBlock.breakBlock()
        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, breakBlock1).getOrThrow()
        addTimeBlock(updatedSession, breakBlock2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when position is -1, block should be added at the end`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()

        // when
        session = addTimeBlock(session, workBlock, -1).getOrThrow()
        session = addTimeBlock(session, breakBlock, -1).getOrThrow()

        // then
        assertEquals(2, session.timeBlocks.size)
        assertEquals(workBlock, session.timeBlocks[0])
        assertEquals(breakBlock, session.timeBlocks[1])
    }

    @Test
    fun `when position is set, should shift all time blocks to the right of the new block`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()

        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        session = addTimeBlock(session, breakBlock).getOrThrow()
        session = addTimeBlock(session, workBlock2, 0).getOrThrow()

        // then
        assertEquals(3, session.timeBlocks.size)
        assertEquals(workBlock2, session.timeBlocks[0])
        assertEquals(workBlock, session.timeBlocks[1])
        assertEquals(breakBlock, session.timeBlocks[2])
    }

    @Test(expected = SessionException.MaxConsecutiveDeepWorkDurationReached::class)
    fun `when consecutive work blocks exceed MAX_CONSECUTIVE_WORK_DURATION, should throw exception`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock(
            duration = 2.hours
        )

        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        addTimeBlock(updatedSession, workBlock2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.InvalidTimeBlockPosition::class)
    fun `when position is less than -1, should throw exception`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        session = addTimeBlock(session, breakBlock).getOrThrow()
        val result = addTimeBlock(session, workBlock2, -2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.InvalidTimeBlockPosition::class)
    fun `when position is greater than the size of the timeBlocks list minus 1, should throw exception`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        session = addTimeBlock(session, breakBlock).getOrThrow()
        addTimeBlock(session, workBlock2, 2).getOrThrow()

        // then
        // exception should be thrown
    }

    private fun createDefaultSession(): ScheduledSession {
        return ScheduledSession.create(
            name = "Morning Session",
            startTime = System.currentTimeMillis(),
        )
    }
}