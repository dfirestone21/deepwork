package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.ScheduledSession
import com.example.deepwork.domain.model.ScheduledSession.Companion.MAX_TIME_BLOCKS
import com.example.deepwork.domain.model.ScheduledTimeBlock
import com.example.deepwork.domain.model.ScheduledTimeBlock.*
import javax.inject.Inject
import kotlin.time.Duration

class AddTimeBlockUseCase @Inject constructor() {

    companion object {
        const val POSITION_DEFAULT = -1
    }

    suspend operator fun invoke(session: ScheduledSession, timeBlock: ScheduledTimeBlock, position: Int = POSITION_DEFAULT): Result<ScheduledSession> {
        return try {
            validate(session, timeBlock, position)
            val timeBlocks = addTimeBlock(session, timeBlock, position)
            val updatedSession = session.copy(timeBlocks = timeBlocks)
            Result.Success(updatedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }

    }
    
    private fun validate(session: ScheduledSession, timeBlock: ScheduledTimeBlock, position: Int) {
        val invalidPosition = position < POSITION_DEFAULT || position > session.timeBlocks.size - 1
        if (invalidPosition) {
            throw SessionException.InvalidTimeBlockPosition()
        }
        if (session.timeBlocks.size >= MAX_TIME_BLOCKS) {
            throw SessionException.MaxTimeBlocksReached()
        }
        val cantStartWithBreakBlock = timeBlock.isBreakBlock && session.timeBlocks.isEmpty()
        if (cantStartWithBreakBlock) {
            throw SessionException.InvalidBreakPosition()
        }
        val previousBlock = previousBlock(session, position)
        val nextBlock = nextBlock(session, position)

        val hasAdjacentBreakBlock = previousBlock?.isBreakBlock == true || nextBlock?.isBreakBlock == true
        val consecutiveBreakBlocks = timeBlock.isBreakBlock && hasAdjacentBreakBlock
        if (consecutiveBreakBlocks) {
            throw SessionException.ConsecutiveBlockTypes()
        }
        val maxSessionDurationReached = session.totalDuration + timeBlock.duration > ScheduledSession.MAX_DURATION
        if (maxSessionDurationReached) {
            throw SessionException.MaxSessionDurationReached()
        }
        if (timeBlock.isWorkBlock) {
            val consecutiveWorkDuration = calculateConsecutiveWorkDuration(session, position, timeBlock)
            if (consecutiveWorkDuration > ScheduledSession.DURATION_MAX_CONSECUTIVE_DEEP_WORK) {
                throw SessionException.MaxConsecutiveDeepWorkDurationReached()
            }
        }
    }

    private fun calculateConsecutiveWorkDuration(session: ScheduledSession, position: Int, newBlock: ScheduledTimeBlock): Duration {
        val blocks = addTimeBlock(session, newBlock, position)
        var maxConsecutiveDuration = Duration.ZERO
        var currentConsecutiveDuration = Duration.ZERO

        blocks.forEach { block ->
            when {
                block.isWorkBlock -> {
                    currentConsecutiveDuration += block.duration
                    if (currentConsecutiveDuration > maxConsecutiveDuration) {
                        maxConsecutiveDuration = currentConsecutiveDuration
                    }
                }
                else -> currentConsecutiveDuration = Duration.ZERO
            }
        }

        return maxConsecutiveDuration
    }

    private fun previousBlock(session: ScheduledSession, position: Int): ScheduledTimeBlock? {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks.lastOrNull()
        } else {
            session.timeBlocks.getOrNull(position - 1)
        }
    }

    private fun nextBlock(session: ScheduledSession, position: Int): ScheduledTimeBlock? {
        return if (position == POSITION_DEFAULT) {
            null
        } else {
            session.timeBlocks.getOrNull(position + 1)
        }
    }

    private fun addTimeBlock(session: ScheduledSession, timeBlock: ScheduledTimeBlock, position: Int): List<ScheduledTimeBlock> {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks + timeBlock
        } else {
            session.timeBlocks.toMutableList().apply { add(position, timeBlock) }
        }
    }
}