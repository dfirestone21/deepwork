package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.TimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AddTimeBlockUseCaseTest {
    private lateinit var addTimeBlock: AddTimeBlockUseCase
    private lateinit var session: Session

    @Before
    fun setup() {
        addTimeBlock = AddTimeBlockUseCase()
        session = createDefaultSession()
    }

    @Test
    fun `when adding new work block, new session should contain work block`() = runTest {
        // given
        val block = TimeBlock.workBlock()
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
            val block = TimeBlock.workBlock()
            val breakBlock = TimeBlock.breakBlock()
            session = addTimeBlock(session, block).getOrThrow()
            session = addTimeBlock(session, breakBlock).getOrThrow()
        }
        val newBlock = TimeBlock.workBlock()
        // when
        addTimeBlock(session, newBlock).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.ConsecutiveWorkBlocks::class)
    fun `when adding a work block directly after another work block, should throw exception`() = runTest {
        // given
        val block1 = TimeBlock.workBlock()
        val block2 = TimeBlock.workBlock()
        val updatedSession = addTimeBlock(session, block1).getOrThrow()
        // when
        addTimeBlock(updatedSession, block2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when adding a break block, new session should contain new break block`() = runTest {
        // given
        val workBlock = TimeBlock.workBlock()
        val breakBlock = TimeBlock.breakBlock()
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
        val breakBlock = TimeBlock.breakBlock()
        // when
        addTimeBlock(session, breakBlock).getOrThrow()

        // then
        // exception should be thrown
    }


    @Test(expected = SessionException.MaxSessionDurationReached::class)
    fun `when adding a block exceeds MAX_DURATION, should throw exception`() = runTest {
        // given
        val block = TimeBlock.workBlock(
            duration = 2.hours
        )
        val break1 = TimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block2 = TimeBlock.workBlock(
            duration = 2.hours
        )
        val break2 = TimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block3 = TimeBlock.workBlock(
            duration = 2.hours
        )
        val break3 = TimeBlock.breakBlock(
            duration = 30.minutes
        )
        val block4 = TimeBlock.workBlock(
            duration = 2.hours
        )
        val break4 = TimeBlock.breakBlock(
            duration = 35.minutes
        )
        val block5 = TimeBlock.workBlock(
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

    @Test(expected = SessionException.InvalidBreakPosition::class)
    fun `when adding a break block directly after another break block, should throw exception`() = runTest {
        // given
        val workBlock = TimeBlock.workBlock()
        val breakBlock1 = TimeBlock.breakBlock()
        val breakBlock2 = TimeBlock.breakBlock()
        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, breakBlock1).getOrThrow()
        addTimeBlock(updatedSession, breakBlock2).getOrThrow()

        // then
        // exception should be thrown
    }

    private fun createDefaultSession(): Session {
        return Session(
            id = "1",
            name = "Morning Focus",
            description = "First session",
            timeBlocks = emptyList()
        )
    }
}