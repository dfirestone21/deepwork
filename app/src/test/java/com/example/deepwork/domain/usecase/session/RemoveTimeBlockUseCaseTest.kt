package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledTimeBlock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveTimeBlockUseCaseTest {

    private lateinit var removeTimeBlock: RemoveTimeBlockUseCase
    private lateinit var session: ScheduledSession

    @Before
    fun setUp() {
        removeTimeBlock = RemoveTimeBlockUseCase()
        session = ScheduledSession.create(
            name = "Test Session",
            startTime = System.currentTimeMillis()
        )
    }

    // -------------------------------------------------------------------------
    // Test 1: Removing an existing work block from a multi-block session
    // -------------------------------------------------------------------------

    @Test
    fun `when removing an existing work block from a multi-block session, should return session without that block`() = runTest {
        // given
        val block0 = ScheduledTimeBlock.deepWorkBlock()
        val block1 = ScheduledTimeBlock.shallowWorkBlock()
        val multiBlockSession = session.copy(timeBlocks = listOf(block0, block1))

        // when
        val result = removeTimeBlock(multiBlockSession, block0.id)

        // then
        val updatedSession = result.getOrThrow()
        assertEquals(1, updatedSession.timeBlocks.size)
        assertTrue(updatedSession.timeBlocks.none { it.id == block0.id })
        assertTrue(updatedSession.timeBlocks.any { it.id == block1.id })
    }

    // -------------------------------------------------------------------------
    // Test 2: Removing the only work block is disallowed
    // -------------------------------------------------------------------------

    @Test
    fun `when removing the only work block in a session, should return MinTimeBlocksReached error`() = runTest {
        // given
        val onlyBlock = ScheduledTimeBlock.deepWorkBlock()
        val singleBlockSession = session.copy(timeBlocks = listOf(onlyBlock))

        // when
        val result = removeTimeBlock(singleBlockSession, onlyBlock.id)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected MinTimeBlocksReached but got ${error.exception}",
            error.exception is SessionException.MinTimeBlocksReached
        )
    }

    // -------------------------------------------------------------------------
    // Test 3: Removing a BREAK block is always valid as long as work blocks remain
    // -------------------------------------------------------------------------

    @Test
    fun `when removing a break block with work blocks remaining, should return session without that break block`() = runTest {
        // given — [WORK_0, BREAK, WORK_1]
        val work0 = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val work1 = ScheduledTimeBlock.shallowWorkBlock()
        val sessionWithBreak = session.copy(timeBlocks = listOf(work0, breakBlock, work1))

        // when
        val result = removeTimeBlock(sessionWithBreak, breakBlock.id)

        // then
        val updatedSession = result.getOrThrow()
        assertEquals(2, updatedSession.timeBlocks.size)
        assertTrue(updatedSession.timeBlocks.none { it.id == breakBlock.id })
        assertTrue(updatedSession.timeBlocks.any { it.id == work0.id })
        assertTrue(updatedSession.timeBlocks.any { it.id == work1.id })
    }

    // -------------------------------------------------------------------------
    // Test 4: Removing a block whose ID doesn't exist returns InvalidTimeBlockPosition
    // -------------------------------------------------------------------------

    @Test
    fun `when removing a block with an id not in the session, should return InvalidTimeBlockPosition error`() = runTest {
        // given
        val existingBlock = ScheduledTimeBlock.deepWorkBlock()
        val nonExistentBlock = ScheduledTimeBlock.deepWorkBlock()
        val singleBlockSession = session.copy(timeBlocks = listOf(existingBlock))

        // when
        val result = removeTimeBlock(singleBlockSession, nonExistentBlock.id)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected InvalidTimeBlockPosition but got ${error.exception}",
            error.exception is SessionException.InvalidTimeBlockPosition
        )
    }

    // -------------------------------------------------------------------------
    // Test 5: Removing a work block that would leave a BREAK at the first position
    // -------------------------------------------------------------------------

    @Test
    fun `when removing first work block would leave a break at first position, should return InvalidBreakPosition error`() = runTest {
        // given — [WORK_0, BREAK, WORK_1]; removing WORK_0 yields [BREAK, WORK_1] — invalid
        val work0 = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val work1 = ScheduledTimeBlock.shallowWorkBlock()
        val sessionToTest = session.copy(timeBlocks = listOf(work0, breakBlock, work1))

        // when
        val result = removeTimeBlock(sessionToTest, work0.id)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected InvalidBreakPosition but got ${error.exception}",
            error.exception is SessionException.InvalidBreakPosition
        )
    }

    // -------------------------------------------------------------------------
    // Test 6: Removing a work block that would leave a BREAK at the last position
    // -------------------------------------------------------------------------

    @Test
    fun `when removing last work block would leave a break at last position, should return InvalidBreakPosition error`() = runTest {
        // given — [WORK_0, BREAK, WORK_1]; removing WORK_1 yields [WORK_0, BREAK] — invalid
        val work0 = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        val work1 = ScheduledTimeBlock.shallowWorkBlock()
        val sessionToTest = session.copy(timeBlocks = listOf(work0, breakBlock, work1))

        // when
        val result = removeTimeBlock(sessionToTest, work1.id)

        // then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(
            "Expected InvalidBreakPosition but got ${error.exception}",
            error.exception is SessionException.InvalidBreakPosition
        )
    }

    // -------------------------------------------------------------------------
    // Bonus: Removing a middle work block from three work blocks succeeds
    // -------------------------------------------------------------------------

    @Test
    fun `when removing a middle work block from a three-work-block session, should return session without that block`() = runTest {
        // given — [WORK_0, WORK_1, WORK_2]
        val work0 = ScheduledTimeBlock.deepWorkBlock()
        val work1 = ScheduledTimeBlock.shallowWorkBlock()
        val work2 = ScheduledTimeBlock.deepWorkBlock()
        val threeBlockSession = session.copy(timeBlocks = listOf(work0, work1, work2))

        // when
        val result = removeTimeBlock(threeBlockSession, work1.id)

        // then
        val updatedSession = result.getOrThrow()
        assertEquals(2, updatedSession.timeBlocks.size)
        assertTrue(updatedSession.timeBlocks.none { it.id == work1.id })
        assertTrue(updatedSession.timeBlocks.any { it.id == work0.id })
        assertTrue(updatedSession.timeBlocks.any { it.id == work2.id })
    }
}
