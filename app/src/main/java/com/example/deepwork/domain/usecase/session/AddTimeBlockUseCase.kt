package com.example.deepwork.domain.usecase.session

import com.example.deepwork.domain.exception.SessionException
import com.example.deepwork.domain.model.Result
import com.example.deepwork.domain.model.Session
import com.example.deepwork.domain.model.Session.Companion.MAX_TIME_BLOCKS
import com.example.deepwork.domain.model.TimeBlock
import com.example.deepwork.domain.model.TimeBlock.*
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AddTimeBlockUseCase @Inject constructor() {

    companion object {
        const val POSITION_DEFAULT = -1
    }

    suspend operator fun invoke(session: Session, timeBlock: TimeBlock, position: Int = POSITION_DEFAULT): Result<Session> {
        return try {
            validate(session, timeBlock, position)
            val timeBlocks = addTimeBlock(session, timeBlock, position)
            val updatedSession = session.copy(timeBlocks = timeBlocks)
            Result.Success(updatedSession)
        } catch (e: Exception) {
            Result.Error(e)
        }

    }
    
    private fun validate(session: Session, timeBlock: TimeBlock, position: Int) {
        val invalidPosition = position < POSITION_DEFAULT || position > session.timeBlocks.size - 1
        if (invalidPosition) {
            throw SessionException.InvalidTimeBlockPosition()
        }
        if (session.timeBlocks.size >= MAX_TIME_BLOCKS) {
            throw SessionException.MaxTimeBlocksReached()
        }
        val cantStartWithBreakBlock = timeBlock is BreakBlock && session.timeBlocks.isEmpty()
        if (cantStartWithBreakBlock) {
            throw SessionException.InvalidBreakPosition()
        }
        val previousBlock = previousBlock(session, position)
        val nextBlock = nextBlock(session, position)

        val consecutiveBreakBlocks = timeBlock is BreakBlock && (previousBlock is BreakBlock || nextBlock is BreakBlock)
        if (consecutiveBreakBlocks) {
            throw SessionException.ConsecutiveBlockTypes()
        }
        val maxSessionDurationReached = session.totalDuration + timeBlock.duration > Session.MAX_DURATION
        if (maxSessionDurationReached) {
            throw SessionException.MaxSessionDurationReached()
        }
        if (timeBlock is WorkBlock) {
            val consecutiveWorkDuration = calculateConsecutiveWorkDuration(session, position, timeBlock)
            if (consecutiveWorkDuration > Session.DURATION_MAX_CONSECUTIVE_DEEP_WORK) {
                throw SessionException.MaxConsecutiveDeepWorkDurationReached()
            }
        }
    }

    private fun calculateConsecutiveWorkDuration(session: Session, position: Int, newBlock: TimeBlock): Duration {
        val blocks = addTimeBlock(session, newBlock, position)
        var maxConsecutiveDuration = Duration.ZERO
        var currentConsecutiveDuration = Duration.ZERO

        blocks.forEach { block ->
            when (block) {
                is WorkBlock -> {
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

    private fun previousBlock(session: Session, position: Int): TimeBlock? {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks.lastOrNull()
        } else {
            session.timeBlocks.getOrNull(position - 1)
        }
    }

    private fun nextBlock(session: Session, position: Int): TimeBlock? {
        return if (position == POSITION_DEFAULT) {
            null
        } else {
            session.timeBlocks.getOrNull(position + 1)
        }
    }

    private fun addTimeBlock(session: Session, timeBlock: TimeBlock, position: Int): List<TimeBlock> {
        return if (position == POSITION_DEFAULT) {
            session.timeBlocks + timeBlock
        } else {
            session.timeBlocks.toMutableList().apply { add(position, timeBlock) }
        }
    }
}