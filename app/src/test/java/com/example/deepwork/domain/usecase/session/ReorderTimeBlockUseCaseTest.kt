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
import kotlin.uuid.Uuid

class ReorderTimeBlockUseCaseTest {

    private lateinit var reorderTimeBlock: ReorderTimeBlockUseCase

    @Before
    fun setUp() {
        reorderTimeBlock = ReorderTimeBlockUseCase()
    }

    // region happy path

    @Test
    fun `when moving work block to a new position, should return session with blocks in the new order`() = runTest {
        // given — [DEEP, SHALLOW, DEEP]
        val deep1 = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val deep2 = ScheduledTimeBlock.deepWorkBlock(position = 2)
        val session = buildSession(deep1, shallow, deep2)

        // when — move deep1 (currently at index 0) to index 2 → expect [SHALLOW, DEEP, DEEP]
        val result = reorderTimeBlock(session, deep1.id, 2)

        // then
        val updatedSession = result.getOrThrow()
        assertEquals(3, updatedSession.timeBlocks.size)
        assertEquals(shallow.id, updatedSession.timeBlocks[0].id)
        assertEquals(deep1.id, updatedSession.timeBlocks[1].id)
        assertEquals(deep2.id, updatedSession.timeBlocks[2].id)
    }

    @Test
    fun `when moving work block to the same position, should return session unchanged`() = runTest {
        // given — [DEEP, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val session = buildSession(deep, shallow)

        // when — move deep to position 0 (no-op)
        val result = reorderTimeBlock(session, deep.id, 0)

        // then
        val updatedSession = result.getOrThrow()
        assertEquals(2, updatedSession.timeBlocks.size)
        assertEquals(deep.id, updatedSession.timeBlocks[0].id)
        assertEquals(shallow.id, updatedSession.timeBlocks[1].id)
    }

    // endregion

    // region consecutive break invariant

    @Test
    fun `when moving work block would create two adjacent BREAK blocks, should return ConsecutiveBlockTypes error`() = runTest {
        // given — [DEEP, BREAK, SHALLOW, BREAK, DEEP]
        val deep1 = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val break1 = ScheduledTimeBlock.breakBlock(position = 1)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 2)
        val break2 = ScheduledTimeBlock.breakBlock(position = 3)
        val deep2 = ScheduledTimeBlock.deepWorkBlock(position = 4)
        val session = buildSession(deep1, break1, shallow, break2, deep2)

        // when — remove shallow (at index 2), which would make break1 and break2 adjacent
        // moving shallow to position 0 leaves [SHALLOW, DEEP, BREAK, BREAK, DEEP] — two consecutive breaks at 2&3
        val result = reorderTimeBlock(session, shallow.id, 0)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.ConsecutiveBlockTypes)
    }

    // endregion

    // region break edge position invariant

    @Test
    fun `when moving break block to position 0, should return InvalidBreakPosition error`() = runTest {
        // given — [DEEP, BREAK, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val breakBlock = ScheduledTimeBlock.breakBlock(position = 1)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 2)
        val session = buildSession(deep, breakBlock, shallow)

        // when — move break to position 0 (first)
        val result = reorderTimeBlock(session, breakBlock.id, 0)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidBreakPosition)
    }

    @Test
    fun `when moving break block to the last position, should return InvalidBreakPosition error`() = runTest {
        // given — [DEEP, BREAK, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val breakBlock = ScheduledTimeBlock.breakBlock(position = 1)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 2)
        val session = buildSession(deep, breakBlock, shallow)

        // when — move break to position 2 (last)
        val result = reorderTimeBlock(session, breakBlock.id, 2)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidBreakPosition)
    }

    // endregion

    // region out-of-bounds position

    @Test
    fun `when targetPosition is negative, should return InvalidTimeBlockPosition error`() = runTest {
        // given — [DEEP, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val session = buildSession(deep, shallow)

        // when
        val result = reorderTimeBlock(session, deep.id, -1)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidTimeBlockPosition)
    }

    @Test
    fun `when targetPosition equals session timeBlocks size, should return InvalidTimeBlockPosition error`() = runTest {
        // given — [DEEP, SHALLOW]  (size = 2, valid positions 0..1)
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val session = buildSession(deep, shallow)

        // when — position 2 is out of bounds for a 2-element list
        val result = reorderTimeBlock(session, deep.id, 2)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidTimeBlockPosition)
    }

    @Test
    fun `when targetPosition is greater than session timeBlocks size, should return InvalidTimeBlockPosition error`() = runTest {
        // given — [DEEP, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val session = buildSession(deep, shallow)

        // when
        val result = reorderTimeBlock(session, deep.id, 99)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidTimeBlockPosition)
    }

    // endregion

    // region unknown blockId

    @Test
    fun `when blockId does not exist in session, should return InvalidTimeBlockPosition error`() = runTest {
        // given — [DEEP, SHALLOW]
        val deep = ScheduledTimeBlock.deepWorkBlock(position = 0)
        val shallow = ScheduledTimeBlock.shallowWorkBlock(position = 1)
        val session = buildSession(deep, shallow)
        val unknownId = Uuid.random()

        // when
        val result = reorderTimeBlock(session, unknownId, 0)

        // then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is SessionException.InvalidTimeBlockPosition)
    }

    // endregion

    // region helpers

    /**
     * Builds a [ScheduledSession] directly from the supplied blocks, bypassing
     * [AddTimeBlockUseCase] so tests can freely construct edge-case orderings.
     * The blocks are assigned sequential positions matching their list index.
     */
    private fun buildSession(vararg blocks: ScheduledTimeBlock): ScheduledSession {
        val positioned = blocks.mapIndexed { index, block -> block.copy(position = index) }
        return ScheduledSession.create(
            name = "Test Session",
            startTime = System.currentTimeMillis()
        ).copy(timeBlocks = positioned)
    }

    // endregion
}
