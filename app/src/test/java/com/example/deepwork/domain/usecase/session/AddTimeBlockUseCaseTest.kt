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
        // given - add 12 shallow work blocks (no consecutive deep limit applies)
        var session = createDefaultSession()
        repeat(12) {
            session = addTimeBlock(session, ScheduledTimeBlock.shallowWorkBlock()).getOrThrow()
        }
        val newBlock = ScheduledTimeBlock.shallowWorkBlock()
        // when
        addTimeBlock(session, newBlock).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when inserting break block between work blocks, session should contain new break block`() = runTest {
        // given - need at least two work blocks to insert a break between them
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock = ScheduledTimeBlock.breakBlock()
        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, workBlock2).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, breakBlock, 1).getOrThrow()
        val actualBreakBlock = updatedSession.timeBlocks[1]

        // then
        assertEquals(3, updatedSession.timeBlocks.size)
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
        // given - build session approaching 12h limit using shallow work blocks
        // (after consecutive-deep-only fix, shallow blocks don't count toward consecutive deep limit)
        // 11 x SHALLOW_60 = 660 min = 11h, then DEEP_120 brings total to 780 min > 720 min (12h)
        var updatedSession = session
        repeat(11) {
            updatedSession = addTimeBlock(updatedSession, ScheduledTimeBlock.shallowWorkBlock(duration = 60.minutes)).getOrThrow()
        }

        // when - adding DEEP_120 exceeds the 12h limit
        addTimeBlock(updatedSession, ScheduledTimeBlock.deepWorkBlock(duration = 120.minutes)).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.ConsecutiveBlockTypes::class)
    fun `when adding a break block directly after another break block, should throw exception`() = runTest {
        // given - build [DEEP, DEEP] then insert first break between them
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        val breakBlock1 = ScheduledTimeBlock.breakBlock()
        val breakBlock2 = ScheduledTimeBlock.breakBlock()
        // when
        var updatedSession = addTimeBlock(session, workBlock).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, workBlock2).getOrThrow()
        updatedSession = addTimeBlock(updatedSession, breakBlock1, 1).getOrThrow()
        // session is now [workBlock, breakBlock1, workBlock2]
        // inserting breakBlock2 at position 2 makes [workBlock, breakBlock1, breakBlock2, workBlock2] - consecutive
        addTimeBlock(updatedSession, breakBlock2, 2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when position is -1, block should be added at the end`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val shallowBlock = ScheduledTimeBlock.shallowWorkBlock()

        // when
        session = addTimeBlock(session, workBlock, -1).getOrThrow()
        session = addTimeBlock(session, shallowBlock, -1).getOrThrow()

        // then
        assertEquals(2, session.timeBlocks.size)
        assertEquals(workBlock, session.timeBlocks[0])
        assertEquals(shallowBlock, session.timeBlocks[1])
    }

    @Test
    fun `when position is set, should shift all time blocks to the right of the new block`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val shallowBlock = ScheduledTimeBlock.shallowWorkBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()

        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        session = addTimeBlock(session, shallowBlock).getOrThrow()
        session = addTimeBlock(session, workBlock2, 0).getOrThrow()

        // then
        assertEquals(3, session.timeBlocks.size)
        assertEquals(workBlock2, session.timeBlocks[0])
        assertEquals(workBlock, session.timeBlocks[1])
        assertEquals(shallowBlock, session.timeBlocks[2])
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
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        val result = addTimeBlock(session, workBlock2, -2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test(expected = SessionException.InvalidTimeBlockPosition::class)
    fun `when position is greater than the size of the timeBlocks list minus 1, should throw exception`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        val shallowBlock = ScheduledTimeBlock.shallowWorkBlock()
        val workBlock2 = ScheduledTimeBlock.deepWorkBlock()
        // when
        session = addTimeBlock(session, workBlock).getOrThrow()
        session = addTimeBlock(session, shallowBlock).getOrThrow()
        addTimeBlock(session, workBlock2, 2).getOrThrow()

        // then
        // exception should be thrown
    }

    @Test
    fun `when adding break block at POSITION_DEFAULT to session with existing work blocks, should return InvalidBreakPosition error`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        session = addTimeBlock(session, workBlock).getOrThrow()
        val breakBlock = ScheduledTimeBlock.breakBlock()

        // when
        val result = addTimeBlock(session, breakBlock, AddTimeBlockUseCase.POSITION_DEFAULT)

        // then
        assert(result is Result.Error) { "Expected Result.Error but got $result" }
        assert((result as Result.Error).exception is SessionException.InvalidBreakPosition) {
            "Expected InvalidBreakPosition but got ${result.exception}"
        }
    }

    @Test
    fun `when inserting break block at position 0 in a non-empty session, should return InvalidBreakPosition error`() = runTest {
        // given
        val workBlock = ScheduledTimeBlock.deepWorkBlock()
        session = addTimeBlock(session, workBlock).getOrThrow()
        val breakBlock = ScheduledTimeBlock.breakBlock()

        // when - inserting at position 0 would make break the first block, which is invalid
        val result = addTimeBlock(session, breakBlock, 0)

        // then
        assert(result is Result.Error) { "Expected Result.Error but got $result" }
        assert((result as Result.Error).exception is SessionException.InvalidBreakPosition) {
            "Expected InvalidBreakPosition but got ${result.exception}"
        }
    }

    private fun createDefaultSession(): ScheduledSession {
        return ScheduledSession.create(
            name = "Morning Session",
            startTime = System.currentTimeMillis(),
        )
    }
}
